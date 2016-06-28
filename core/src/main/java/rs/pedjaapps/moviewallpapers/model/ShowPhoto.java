package rs.pedjaapps.moviewallpapers.model;

import java.io.Serializable;

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
public class ShowPhoto implements TypeListItem.IListItem, Comparable<ShowPhoto>, Serializable
{
    public static final int VIEW_TYPE_PHOTO = 1;
    public static final int VIEW_TYPE_OVERVIEW = 2;
    public int showId;
    public String filename, type;
    public transient Show show;
    public transient boolean downloading;
    public boolean fullPath;

    private final int viewType;

    public ShowPhoto(int viewType)
    {
        this.viewType = viewType;
    }

    public ShowPhoto()
    {
        viewType = VIEW_TYPE_PHOTO;
    }

    //private
    public int ord;

    public int compareTo(ShowPhoto o)
    {
        return Integer.compare(ord, o.ord);
    }

    @Override
    public int getViewType()
    {
        return viewType;
    }

    @Override
    public long getId()
    {
        return showId;
    }
    //private
}
