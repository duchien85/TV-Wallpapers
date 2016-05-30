package rs.pedjaapps.moviewallpapers.model;

import com.tehnicomsolutions.http.Http;
import com.tehnicomsolutions.http.Request;
import com.tehnicomsolutions.http.RequestHandler;
import com.tehnicomsolutions.http.ResponseParser;

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
public class MyRequestHandler implements RequestHandler
{
    public static final int REQUEST_CODE_LOGIN = 1001;
    public static final int REQUEST_CODE_SHOW = 1002;
    public static final int REQUEST_CODE_GET_IMAGE_PARAMS = 1003;
    public static final int REQUEST_CODE_GET_IMAGES = 1004;
    public static final int REQUEST_CODE_GET_POPULAR_SHOWS = 1005;

    public ResponseParser handleRequest(int requestCode, Request builder, boolean sync)
    {
        JSONParser parser = new JSONParser(Http.getInstance().internet.executeHttpRequest(builder));
        switch (requestCode)
        {
            case REQUEST_CODE_LOGIN:
                parser.parseLoginResponse();
                break;
            case REQUEST_CODE_SHOW:
                parser.parseShowResponse();
                break;
            case REQUEST_CODE_GET_IMAGE_PARAMS:
                parser.parseImageParamsResponse();
                break;
            case REQUEST_CODE_GET_IMAGES:
                parser.parseImagesResponse();
                break;
            case REQUEST_CODE_GET_POPULAR_SHOWS:
                parser.parseGetPopularShows();
                break;
        }
        return parser;
    }

    public void handlePreRequest(int requestCode, boolean sync)
    {

    }

    public void handlePostRequest(int requestCode, Request builder, ResponseParser parser, boolean sync)
    {

    }

    public void handleRequestCancelled(int requestCode, ResponseParser parser, boolean sync)
    {

    }
}
