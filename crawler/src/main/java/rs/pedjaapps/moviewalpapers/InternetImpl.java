package rs.pedjaapps.moviewalpapers;

import com.tehnicomsolutions.http.Http;
import com.tehnicomsolutions.http.HttpUtility;
import com.tehnicomsolutions.http.Internet;
import com.tehnicomsolutions.http.Request;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Copyright (c) 2016 "Predrag Čokulov,"
 * pedjaapps [https://pedjaapps.net]
 * <p>
 * This file is part of Movie Wallpapers.
 * <p>
 * Movie Wallpapers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class InternetImpl extends Internet
{
    private static final String LINE_FEED = "\r\n";

    public Response executeHttpRequest(Request request, boolean streamToString)
    {
        long start = System.currentTimeMillis();
        Response response = new Response();
        InputStream is = null;
        try
        {
            HttpURLConnection conn = (HttpURLConnection) new URL(request.getRequestUrl()).openConnection();
            //conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setRequestMethod(request.getMethod().toString());
            conn.setDoInput(true);

            for (String key : request.getHeaders().keySet())
            {
                conn.setRequestProperty(key, request.getHeaders().get(key));
            }

            switch (request.getMethod())
            {
                case PUT:
                    break;
                case POST:
                    conn.setDoOutput(true);
                    switch (request.getPostMethod())
                    {
                        case BODY:
                            if (request.getRequestBody() == null)
                                throw new IllegalArgumentException("body cannot be null if post method is BODY");
                            conn.setRequestProperty("Content-Type", request.getContentType());
                            conn.setRequestProperty("Content-Length", Integer.toString(request.getRequestBody().getBytes().length));
                            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                            wr.writeBytes(request.getRequestBody());
                            wr.flush();
                            wr.close();
                            break;
                        case X_WWW_FORM_URL_ENCODED:
                            setXWwwFormUrlEncodedParams(conn, request);
                            break;
                        case FORM_DATA:
                            final String BOUNDARY = "===" + System.currentTimeMillis() + "===";
                            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                            OutputStream os = conn.getOutputStream();
                            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, ENCODING), true);
                            for (String key : request.getPOSTParams().keySet())
                            {
                                writer.append("--").append(BOUNDARY).append(LINE_FEED);
                                writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_FEED);
                                writer.append("Content-Type: text/plain; charset=" + ENCODING).append(LINE_FEED);
                                writer.append(LINE_FEED);
                                writer.append(request.getPOSTParams().get(key)).append(LINE_FEED);
                                writer.flush();
                            }

                            if (request.getFiles() != null)
                            {
                                for (int i = 0; i < request.getFiles().length; i++)
                                {
                                    Request.UploadFile file = request.getFiles()[i];

                                    writer.append("--").append(BOUNDARY).append(LINE_FEED);
                                    writer.append("Content-Disposition: form-data; name=\"").append(request.getFileParamName()).append(Integer.toString(i)).append("\"; filename=\"").append(file.fileName).append("\"").append(LINE_FEED);
                                    writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(file.fileName)).append(LINE_FEED);
                                    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                                    writer.append(LINE_FEED);
                                    writer.flush();

                                    InputStream inputStream = createInputStreamFromUploadFile(file);
                                    if(inputStream == null)continue;

                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1)
                                    {
                                        os.write(buffer, 0, bytesRead);
                                    }
                                    os.flush();
                                    inputStream.close();

                                    writer.append(LINE_FEED);
                                    writer.flush();
                                }
                            }
                            writer.append(LINE_FEED).flush();
                            writer.append("--").append(BOUNDARY).append("--").append(LINE_FEED);
                            writer.close();

                            os.close();
                            break;
                    }
                    break;
                case GET:
                    break;
                case DELETE:
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    break;
            }

            conn.connect();

            response.code = conn.getResponseCode();
            if(streamToString)
            {
                response.responseDataString = readStreamToString(is = response.code < 400 ? conn.getInputStream() : conn.getErrorStream());
            }
            response.responseData = is = response.code < 400 ? conn.getInputStream() : conn.getErrorStream();
            response.responseMessage = response.code < 400 ? null : conn.getResponseMessage();
        }
        catch (IOException e)
        {
            response.responseMessage = "Network Error";
            response.responseDetailedMessage = e.getMessage();
        }
        finally
        {
            response.request = request.getRequestUrl();
            if (Http.LOGGING)
                System.out.println("executeHttpRequest[" + request.getRequestUrl() + "]: Took:'" + (System.currentTimeMillis() - start) + "', " + response);
            if (is != null && streamToString)
            {
                try
                {
                    is.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }

        return response;
    }

    private InputStream createInputStreamFromUploadFile(Request.UploadFile file) throws FileNotFoundException
    {
        if (file.uri.startsWith("/"))
        {
            return new FileInputStream(new File(file.uri));
        }
        else if (file.uri.startsWith("file://"))
        {
            return new FileInputStream(new File(file.uri.replace("file://", "")));
        }
        return null;
    }

    private static void setXWwwFormUrlEncodedParams(HttpURLConnection conn, Request requestBuilder) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        for (String key : requestBuilder.getPOSTParams().keySet())
        {
            builder.append("&").append(key).append("=").append(HttpUtility.encodeString(requestBuilder.getPOSTParams().get(key)));
        }
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(builder.toString().getBytes().length));
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(builder.toString());
        wr.flush();
        wr.close();
    }

    public static String readStreamToString(InputStream stream) throws IOException
    {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        StringBuilder string = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null)
        {
            string.append(line);
        }
        return string.toString();
    }
}
