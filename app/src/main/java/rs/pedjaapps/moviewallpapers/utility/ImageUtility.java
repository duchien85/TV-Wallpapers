package rs.pedjaapps.moviewallpapers.utility;

import android.annotation.SuppressLint;

import rs.pedjaapps.moviewallpapers.model.ShowPhoto;

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
public class ImageUtility
{
    @SuppressLint("DefaultLocale")
    public static String generateImageUrlThumb(ShowPhoto photo)
    {
        if(photo == null)
            return null;
        return String.format("http://-pedjaapps.net/mwp_static/images/%d/fanart/thumb_%s", photo.showId, photo.filename);
    }

    @SuppressLint("DefaultLocale")
    public static String generateImageUrlFull(ShowPhoto photo)
    {
        if(photo == null)
            return null;
        return String.format("http://-pedjaapps.net/mwp_static/images/%d/fanart/%s", photo.showId, photo.filename);
    }
}
