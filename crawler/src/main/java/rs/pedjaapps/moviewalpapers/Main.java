package rs.pedjaapps.moviewalpapers;

import com.tehnicomsolutions.http.Http;
import com.tehnicomsolutions.http.Internet;
import com.tehnicomsolutions.http.Network;
import com.tehnicomsolutions.http.Request;
import com.tehnicomsolutions.http.RequestManager;
import com.tehnicomsolutions.http.ResponseParser;
import com.tehnicomsolutions.http.TextManager;
import com.tehnicomsolutions.http.UI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import rs.pedjaapps.moviewallpapers.DatabaseManager;
import rs.pedjaapps.moviewallpapers.model.*;

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
public class Main
{
    private static final AtomicInteger ids = new AtomicInteger(0);
    private static int lastId;
    private static Integer[] invalidIds;
    private static BlockingDeque<Integer> downloadIds;

    public static void main(String[] args) throws IOException
    {
        if(args.length < 1)
        {
            throw new RuntimeException("first argument must be type of operation");
        }
        Network network = new NetworkImpl();
        Internet internet = new InternetImpl();
        UI ui = new UIImpl();
        TextManager textManager = new TextManagerImpl();

        Http.initialize(network, internet, ui, textManager);
        Http.LOGGING = true;

        RequestManager.initialize(new RequestManager()
        {
            @Override
            protected void runOnUIThread(Runnable runnable)
            {
                runnable.run();
            }
        });

        DatabaseManager.init();
        RequestManager.getInstance().setGlobalRequestHandler(new rs.pedjaapps.moviewallpapers.model.MyRequestHandler());

        if("get_shows".equals(args[0]))
        {
            String token = initTvdb();
            invalidIds = DatabaseManager.getInstance().getInvalidIds();
            lastId = DatabaseManager.getInstance().getLastShowId();
            System.out.println("invalidIds:" + invalidIds.length);
            for (int i = 0; i < 16; i++)
            {
                new Worker(token, false).start();
            }
        }
        else if("download_images".equals(args[0]))
        {
            String token = initTvdb();
            downloadIds = new LinkedBlockingDeque<>();
            downloadIds.addAll(Arrays.asList(DatabaseManager.getInstance().getShowIds()));
            invalidIds = DatabaseManager.getInstance().getInvalidIdsForDownload("poster");

            for (int i = 0; i < 16; i++)
            {
                new Worker(token, true).start();
            }
        }
        else if("get_popular_movies".equals(args[0]))
        {
            Request request = new Request(Request.Method.GET);
            request.setRequestUrl("https://api-v2launch.trakt.tv/shows/popular");
            request.addHeader("Content-Type", "application/json");
            request.addHeader("trakt-api-version", "2");
            request.addHeader("trakt-api-key", "21d99c083e81a2906244853ecec854b15ae2a50cc9387ce337f26165f1f10995");
            request.addParam("limit", "100");

            ResponseParser responseParser = RequestManager.getInstance().executeSync(request, rs.pedjaapps.moviewallpapers.model.MyRequestHandler.REQUEST_CODE_GET_POPULAR_SHOWS);
            if(responseParser.getParseObject() != null)
            {
                List<Integer> ids = responseParser.getParseObject();
                if(!ids.isEmpty())
                {
                    DatabaseManager.getInstance().deleteAll("popular_shows");
                    DatabaseManager.getInstance().insertPopularShows(ids);
                }
            }
        }





    }

    private static String initTvdb()
    {
        Request.setDefaultRequestUrl("https://api.thetvdb.com");

        Request loginRequest = new Request(Request.Method.POST);
        loginRequest.addUrlPart("/login");
        loginRequest.setPostMethod(Request.PostMethod.BODY);
        loginRequest.setContentType("application/json");
        loginRequest.setRequestBody("{\"apikey\":\"E4BD239A1D7130F7\"}");

        ResponseParser loginParser = RequestManager.getInstance().executeSync(loginRequest, rs.pedjaapps.moviewallpapers.model.MyRequestHandler.REQUEST_CODE_LOGIN);
        if (loginParser.getParseObject() == null)
        {
            throw new RuntimeException("Failed to login");
        }
        return loginParser.getParseObject();
    }

    private static class Worker extends Thread
    {
        String token;
        boolean downloadImagesForIds;

        Worker(String token, boolean downloadImagesForIds)
        {
            this.token = token;
            this.downloadImagesForIds = downloadImagesForIds;
        }

        @Override
        public void run()
        {
            if (!downloadImagesForIds)
            {
                int id = 0;
                while (id < 320000)
                {
                    synchronized (ids)
                    {
                        id = ids.incrementAndGet();
                    }
                    System.out.print(id);
                    int found = Arrays.binarySearch(invalidIds, id);
                    if (found >= 0)
                    {
                        System.out.println(": skipped");
                        continue;
                    }
                    Request request = new Request(Request.Method.GET);
                    request.addUrlPart("series").addUrlPart(String.valueOf(id));
                    request.addHeader("Authorization", "Bearer " + token);

                    ResponseParser parser = RequestManager.getInstance().executeSync(request, rs.pedjaapps.moviewallpapers.model.MyRequestHandler.REQUEST_CODE_SHOW);
                    if(parser.getParseObject() instanceof Show)
                    {
                        Show show = parser.getParseObject();
                        int status = DatabaseManager.getInstance().insertShow(show);
                        if (status < 0)
                        {
                            System.out.println(show.id);
                            System.out.println(show.imdb);
                            throw new RuntimeException("Failed to insert valid movie");
                        }
                    }
                    System.out.println(": success: " + parser.getParseObject());
                    //not found, and id is less than last movie id, add it to ignore list
                    if (parser.getServerResponse() != null && parser.getServerResponse().code == 404)
                    {
                        if (lastId > 0 && id < lastId)
                        {
                            DatabaseManager.getInstance().insertInvalidId(id);
                        }
                    }
                    if (parser.getParseObject() != null)
                    {
                        //success
                        //download photos
                        downloadImages(id, token);
                    }
                }
            }
            else
            {
                while(!downloadIds.isEmpty())
                {
                    int id = downloadIds.remove();
                    System.out.print(id);
                    int found = Arrays.binarySearch(invalidIds, id);
                    if (found >= 0)
                    {
                        System.out.println(": skipped");
                        continue;
                    }
                    downloadImages(id, token);
                }
            }
        }
    }

    private static final File IMAGES_ROOT = new File("/var/www/vhosts/pedjaapps.net/httpdocs/mwp_static/images");
    private static final String URL_PREFIX = "https://thetvdb.com/banners/";

    private static void downloadImages(int id, String token)
    {
        File showRoot = new File(IMAGES_ROOT, String.valueOf(id));
        showRoot.mkdir();

        File fanart = new File(showRoot, "poster");
        fanart.mkdir();

        Request paramsRequest = new Request(Request.Method.GET);
        paramsRequest.addUrlPart("series").addUrlPart(String.valueOf(id)).addUrlPart("images").addUrlPart("query").addUrlPart("params");
        paramsRequest.addHeader("Authorization", "Bearer " + token);

        Request request = new Request(Request.Method.GET);
        request.addUrlPart("series").addUrlPart(String.valueOf(id)).addUrlPart("images").addUrlPart("query");
        request.addParam("keyType", "poster");
        request.addHeader("Authorization", "Bearer " + token);
        ResponseParser parser = RequestManager.getInstance().executeSync(request, rs.pedjaapps.moviewallpapers.model.MyRequestHandler.REQUEST_CODE_GET_IMAGES);
        if (parser.getParseObject() != null && !parser.getParseObject(List.class).isEmpty())
        {
            List<String> images = parser.getParseObject();
            for (String image : images)
            {
                String urlFull = URL_PREFIX + image;
                String urlThumb = URL_PREFIX + "_cache/" + image;
                String fileName = FilenameUtils.getName(image);
                File imageFile = new File(fanart, fileName);
                File imageFileThumb = new File(fanart, "thumb_" + fileName);

                ShowPhoto showPhoto = new ShowPhoto();
                showPhoto.showId = id;
                showPhoto.filename = fileName;
                showPhoto.type = "poster";
                DatabaseManager.getInstance().insertShowPhoto(showPhoto);

                if (!imageFile.exists())
                {
                    Request downloadRequest = new Request(Request.Method.GET);
                    downloadRequest.setRequestUrl(urlFull);

                    Internet.Response response = Http.getInstance().internet.executeHttpRequest(downloadRequest, false);
                    try
                    {
                        FileUtils.copyInputStreamToFile((InputStream) response.responseData, imageFile);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }

                if (!imageFileThumb.exists())
                {
                    Request downloadRequestThumb = new Request(Request.Method.GET);
                    downloadRequestThumb.setRequestUrl(urlThumb);

                    Internet.Response response = Http.getInstance().internet.executeHttpRequest(downloadRequestThumb, false);
                    try
                    {
                        FileUtils.copyInputStreamToFile((InputStream) response.responseData, imageFileThumb);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        else if(parser.getServerResponse() != null && parser.getServerResponse().code == 404)
        {
            DatabaseManager.getInstance().insertPhotoInvalidId(id);
        }

    }
}
