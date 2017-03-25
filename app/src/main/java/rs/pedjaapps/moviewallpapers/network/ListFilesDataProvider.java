package rs.pedjaapps.moviewallpapers.network;

import com.androidforever.dataloader.DataProvider;

import java.util.ArrayList;
import java.util.List;

import rs.pedjaapps.moviewallpapers.model.Page;
import rs.pedjaapps.moviewallpapers.model.ShowPhoto;
import rs.pedjaapps.moviewallpapers.model.TypeListItem;
import rs.pedjaapps.moviewallpapers.utility.DownloadUtils;

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
public class ListFilesDataProvider implements DataProvider<Page<ShowPhoto>>
{
    private Page<ShowPhoto> page;

    @Override
    public boolean load()
    {
        List<ShowPhoto> photos = DownloadUtils.listDownloadePhotos();

        page = new Page<>();
        page.next = false;
        page.page = 1;
        page.count = photos.size();
        page.items = new ArrayList<>();

        for(ShowPhoto photo : photos)
        {
            page.items.add(new TypeListItem<>(photo));
        }

        return true;
    }

    @Override
    public Page<ShowPhoto> getResult()
    {
        return page;
    }

    @Override
    public boolean forceLoading()
    {
        return false;
    }
}
