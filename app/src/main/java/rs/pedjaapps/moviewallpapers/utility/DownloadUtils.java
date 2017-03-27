package rs.pedjaapps.moviewallpapers.utility;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

import org.skynetsoftware.jutils.Alert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rs.pedjaapps.moviewallpapers.App;
import rs.pedjaapps.moviewallpapers.R;
import rs.pedjaapps.moviewallpapers.model.Show;
import rs.pedjaapps.moviewallpapers.model.ShowPhoto;
import rs.pedjaapps.moviewallpapers.model.TextUtils;

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
public class DownloadUtils
{
    private DownloadUtils()
    {

    }

    public static void download(ShowPhoto showPhoto)
    {
        String url;
        if(showPhoto == null || showPhoto.show == null || TextUtils.isEmpty((url = ImageUtility.generateImageUrlFull(showPhoto))))
        {
            Alert.showToast(App.get(), R.string.download_failed);
            return;
        }
        String fileName = generateFileName(showPhoto);
        File file = new File(App.get().DOWNLOAD_DIR, fileName);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationUri(Uri.fromFile(file));

        DownloadManager manager = (DownloadManager) App.get().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private static String generateFileName(ShowPhoto showPhoto)
    {
        return String.format("%s<->%s", showPhoto.show.title, showPhoto.filename);
    }

    public static boolean isPhotoDownloaded(ShowPhoto showPhoto)
    {
        if(showPhoto == null || showPhoto.show == null)
            return false;
        String fileName = generateFileName(showPhoto);
        File file = new File(App.get().DOWNLOAD_DIR, fileName);
        return file.exists();
    }

    private static final Pattern FILENAME_PATTERN = Pattern.compile("(.+)\\<-\\>(.+)");

    public static List<ShowPhoto> listDownloadePhotos()
    {
        File[] files = App.get().DOWNLOAD_DIR.listFiles();
        List<ShowPhoto> photos = new ArrayList<>(files.length);

        Matcher matcher = FILENAME_PATTERN.matcher("");

        for(File file : files)
        {
            matcher.reset(file.getName());
            if(!matcher.matches())
                continue;
            String showName = matcher.group(1);
            Show show = new Show();
            show.title = showName;

            ShowPhoto showPhoto = new ShowPhoto();
            showPhoto.fullPath = true;
            showPhoto.filename = file.getAbsolutePath();
            showPhoto.show = show;
            photos.add(showPhoto);
        }
        return photos;
    }
}
