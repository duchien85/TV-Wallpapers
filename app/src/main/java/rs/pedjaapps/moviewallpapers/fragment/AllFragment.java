package rs.pedjaapps.moviewallpapers.fragment;

import com.androidforever.dataloader.DataProvider;
import com.tehnicomsolutions.http.Request;

import java.util.List;

import rs.pedjaapps.moviewallpapers.model.Page;
import rs.pedjaapps.moviewallpapers.model.ShowPhoto;
import rs.pedjaapps.moviewallpapers.network.NetworkDataProvider;

/**
 * Copyright (c) 2016 "Predrag Čokulov,"
 * pedjaapps [https://pedjaapps.net]
 * <p>
 * This file is part of MovieWallpapers.
 * <p>
 * MovieWallpapers is free software: you can redistribute it and/or modify
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
public class AllFragment extends PhotoGridFragment
{
    @Override
    protected List<DataProvider<Page<ShowPhoto>>> getAdditionalProviders()
    {
        return null;
    }

    @Override
    protected Request getRequest()
    {
        Request request = new Request(Request.Method.GET);
        request.addUrlPart("shows").addUrlPart("list");
        request.addParam("with_photo", String.valueOf(true));
        return request;
    }

    @Override
    protected int getRequestCode()
    {
        return NetworkDataProvider.REQUEST_CODE_SHOWS_AS_PHOTOS;
    }
}
