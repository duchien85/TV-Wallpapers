package rs.pedjaapps.moviewallpapers.model;

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
public class ShowPhoto implements TypeListItem.IListItem, Comparable<ShowPhoto>
{
    public static final int VIEW_TYPE_PHOTO = 1;
    public int showId;
    public String filename, type;
    public Show show;

    //private
    public int ord;

    public int compareTo(ShowPhoto o)
    {
        return Integer.compare(ord, o.ord);
    }

    @Override
    public int getViewType()
    {
        return VIEW_TYPE_PHOTO;
    }

    @Override
    public long getId()
    {
        return showId;
    }
    //private
}
