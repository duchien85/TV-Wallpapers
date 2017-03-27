package rs.pedjaapps.moviewallpapers.model;


import org.json.JSONArray;
import org.json.JSONObject;
import org.skynetsoftware.snet.Internet;
import org.skynetsoftware.snet.ResponseParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by pedja on 3.7.15. 10.38.
 * This class is part of the SBBet
 * Copyright © 2015 ${OWNER}
 */
public class JSONParser extends ResponseParser
{
    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private int responseStatusCode = -1;
    private String responseMessage;

    public JSONParser(String stringObject)
    {
        super(stringObject);
        init();
    }

    public JSONParser(Internet.Response serverResponse)
    {
        super(serverResponse);
        init();
    }

    private void init()
    {
        try
        {
            fixResponse();
            if (serverResponse.responseDataString.startsWith("{"))
            {
                jsonObject = new JSONObject(this.serverResponse.responseDataString);
            }
            else
            {
                jsonArray = new JSONArray(this.serverResponse.responseDataString);
            }
            checkErrors();
            if (!serverResponse.isResponseOk())
            {
                jsonObject = null;
            }
        }
        catch (Exception e)
        {
            System.out.println("JSONUtility " + e.getMessage());
            System.out.println("JSONUtility :: Failed to parse json");
            System.out.println(serverResponse.responseMessage);
            System.out.println(serverResponse.responseDetailedMessage);
            System.out.println(serverResponse.responseDataString);
            System.out.println(serverResponse.code);
            System.out.println(serverResponse.request);
            e.printStackTrace();
        }
    }

    /**
     * Check if there are any errors (server errors) in json response
     */
    private void checkErrors()
    {
        if (jsonObject == null) return;
        String status;
        responseStatusCode = "OK".equals((status = jsonObject.optString("status", "OK"))) ? ResponseParser.RESPONSE_STATUS_SUCCESS : ResponseParser.RESPONSE_STATUS_RESPONSE_ERROR;//assume success if no error
        if (responseStatusCode == ResponseParser.RESPONSE_STATUS_SUCCESS)
        {
            String code = jsonObject.optString("code");
            if (!TextUtils.isEmpty(code))
            {
                responseStatusCode = ResponseParser.RESPONSE_STATUS_RESPONSE_ERROR;
                status = jsonObject.optString("message");
            }
        }
        if (responseStatusCode != ResponseParser.RESPONSE_STATUS_SUCCESS)
        {
            setResponseMessage(status);
        }
        if (responseStatusCode != ResponseParser.RESPONSE_STATUS_SUCCESS)
        {
            jsonObject = null;
        }
    }

    private void setResponseMessage(String status)
    {
        if (TextUtils.isEmpty(status))
            return;
        responseMessage = status;
    }

    /**
     * Sometimes shit happens so server returns some "Notices" and/or "Warnings"
     * This method tries to fix that
     * This method assumes that there are no json object/array("{,},[,]") characters before json content starts
     * <p/>
     * This is for debugging only while app is still in development
     * <p/>
     * This method is not error-proof, it wont handle all scenarios where response is wrong
     */
    private void fixResponse()
    {
        if (serverResponse == null || serverResponse.responseData == null || serverResponse.responseDataString.startsWith("{") || serverResponse.responseDataString.startsWith("["))//response is ok
            return;

        //response is not json or it has php warnings/notices before json response. try to fix it

        int jsonStartIndex = serverResponse.responseDataString.indexOf("{");//first assume root element is json object

        if (jsonStartIndex == -1)//if its -1, try with "["
        {
            jsonStartIndex = serverResponse.responseDataString.indexOf("[");
            if (jsonStartIndex != -1)
            {
                //fix it
                serverResponse.responseData = serverResponse.responseDataString.substring(jsonStartIndex, serverResponse.responseDataString.length());
            }
        }
        else
        {
            //fix it
            serverResponse.responseData = serverResponse.responseDataString.substring(jsonStartIndex, serverResponse.responseDataString.length());
        }
    }

    @Override
    public int getResponseStatus()
    {
        if (responseStatusCode == -1)
        {
            return super.getResponseStatus();
        }
        else
        {
            return responseStatusCode;
        }
    }

    @Override
    public String getResponseMessage()
    {
        if (responseMessage == null)
        {
            return serverResponse.responseMessage;
        }
        else
        {
            return responseMessage;
        }
    }

    public void parseLoginResponse()
    {
        if (jsonObject == null)
        {
            return;
        }
        parseObject = jsonObject.optString("token");
    }

    private static final SimpleDateFormat firstAiredFormat = new SimpleDateFormat("yyyy-MM-dd");

    public void parseShowResponse()
    {
        if (jsonObject == null)
        {
            return;
        }
        JSONObject jData = jsonObject.optJSONObject("data");
        if (jData != null)
        {
            Show show = new Show();
            show.id = jData.optInt("id");
            show.title = jData.optString("seriesName");
            String firstAired = jData.optString("firstAired");
            try
            {
                Date date = firstAiredFormat.parse(firstAired);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                show.year = calendar.get(Calendar.YEAR);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            show.imdb = jData.optString("imdbId");
            show.overview = jData.optString("overview");
            parseObject = show;
        }
    }

    public void parseImageParamsResponse()
    {
        if (jsonObject == null)
            return;
        JSONArray jDatas = jsonObject.optJSONArray("data");
        if (jDatas != null)
        {
            List<String> keys = new ArrayList<String>(jDatas.length());
            for (int i = 0; i < jDatas.length(); i++)
            {
                JSONObject jData = jDatas.optJSONObject(i);
                String key = jData.optString("keyType", null);
                if (key != null)
                {
                    keys.add(key);
                }
            }
            parseObject = keys;
        }
    }

    public void parseImagesResponse()
    {
        if (jsonObject == null)
            return;
        JSONArray jDatas = jsonObject.optJSONArray("data");
        if (jDatas != null)
        {
            List<String> images = new ArrayList<String>(jDatas.length());
            for (int i = 0; i < jDatas.length(); i++)
            {
                JSONObject jData = jDatas.optJSONObject(i);
                String key = jData.optString("fileName", null);
                if (key != null)
                {
                    images.add(key);
                }
            }
            parseObject = images;
        }
    }

    public void parseGetPopularShows()
    {
        if (jsonArray == null)
            return;
        List<Integer> ids = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject jData = jsonArray.optJSONObject(i);
            JSONObject jIds = jData.optJSONObject("ids");
            int id = jIds.optInt("tvdb", -1);
            if (id > 0)
            {
                ids.add(id);
            }
        }
        parseObject = ids;

    }

    public void parseGetShowPhotosResponse()
    {
        if (jsonObject == null)
            return;
        JSONArray jPhotos = jsonObject.optJSONArray("show_photos");
        if (jPhotos != null)
        {
            Page<ShowPhoto> page = new Page<>();
            page.items = new ArrayList<>(jPhotos.length());
            page.next = jsonObject.optBoolean("has_next");
            page.page = jsonObject.optInt("page", 1);

            for (int i = 0; i < jPhotos.length(); i++)
            {
                JSONObject jPhoto = jPhotos.optJSONObject(i);
                ShowPhoto showPhoto = new ShowPhoto();
                showPhoto.filename = jPhoto.optString("filename");
                showPhoto.showId = jPhoto.optInt("show_id");

                JSONObject jShow = jPhoto.optJSONObject("show");
                if (jShow != null)
                {
                    Show show = new Show();
                    show.title = jShow.optString("title");
                    show.year = jShow.optInt("year");
                    showPhoto.show = show;
                }

                page.items.add(new TypeListItem<>(showPhoto));
            }
            parseObject = page;
        }

    }

    public void parseGetShowResponse()
    {
        if (jsonObject == null)
            return;
        Show show = new Show();
        show.title = jsonObject.optString("title");
        show.year = jsonObject.optInt("year");
        show.imdb = jsonObject.optString("imdb_id");
        show.id = jsonObject.optInt("id");
        show.overview = jsonObject.optString("overview");

        ShowPhoto showPhoto = new ShowPhoto(ShowPhoto.VIEW_TYPE_OVERVIEW);
        showPhoto.filename = jsonObject.optString("filename");
        showPhoto.showId = show.id;
        showPhoto.show = show;
        show.showPhoto = showPhoto;

        JSONArray jPhotos = jsonObject.optJSONArray("photos");
        if (jPhotos != null)
        {
            List<TypeListItem<ShowPhoto>> photos = new ArrayList<>();
            photos.add(new TypeListItem<>(showPhoto));
            for (int i = 0; i < jPhotos.length(); i++)
            {
                JSONObject jPhoto = jPhotos.optJSONObject(i);
                showPhoto = new ShowPhoto();
                showPhoto.filename = jPhoto.optString("filename");
                showPhoto.showId = show.id;
                showPhoto.show = show;

                photos.add(new TypeListItem<>(showPhoto));
            }
            show.photos = photos;
        }

        parseObject = show;
    }

    public void parseGetShowsAsPhotosResponse()
    {
        if (jsonObject == null)
            return;
        JSONArray jPhotos = jsonObject.optJSONArray("shows");
        if (jPhotos != null)
        {
            Page<ShowPhoto> page = new Page<>();
            page.items = new ArrayList<>(jPhotos.length());
            page.next = jsonObject.optBoolean("has_next");
            page.page = jsonObject.optInt("page", 1);

            for (int i = 0; i < jPhotos.length(); i++)
            {
                JSONObject jShow = jPhotos.optJSONObject(i);
                ShowPhoto showPhoto = new ShowPhoto();
                showPhoto.filename = jShow.optString("filename");
                showPhoto.showId = jShow.optInt("id");

                Show show = new Show();
                show.title = jShow.optString("title");
                show.year = jShow.optInt("year");
                show.id = showPhoto.showId;
                showPhoto.show = show;

                page.items.add(new TypeListItem<>(showPhoto));
            }
            parseObject = page;
        }

    }
}
