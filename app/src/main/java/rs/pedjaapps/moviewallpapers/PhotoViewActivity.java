package rs.pedjaapps.moviewallpapers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import rs.pedjaapps.moviewallpapers.model.ShowPhoto;
import rs.pedjaapps.moviewallpapers.utility.ImageUtility;
import uk.co.senab.photoview.PhotoView;

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
public class PhotoViewActivity extends AppCompatActivity
{
    private static final String INTENT_EXTRA_PHOTO = "photo";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        final PhotoView ivImage = (PhotoView) findViewById(R.id.ivImageFull);
        final ImageView ivImageTrans = (ImageView) findViewById(R.id.ivImage);

        final ProgressBar pbLoading = (ProgressBar) findViewById(R.id.pbLoading);

        ShowPhoto showPhoto = (ShowPhoto) getIntent().getSerializableExtra(INTENT_EXTRA_PHOTO);
        if(showPhoto == null)
        {
            finish();
        }
        else
        {
            if(showPhoto.fullPath)
            {
                ivImageTrans.setVisibility(View.GONE);
                pbLoading.setVisibility(View.GONE);
                Glide.with(this).load(showPhoto.filename).dontAnimate().into(ivImage);
            }
            else
            {
                Glide.with(this).load(ImageUtility.generateImageUrlThumb(showPhoto)).dontAnimate().into(ivImageTrans);
                Glide.with(this).load(ImageUtility.generateImageUrlThumb(showPhoto)).dontAnimate().listener(new RequestListener<String, GlideDrawable>()
                {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource)
                    {
                        pbLoading.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource)
                    {
                        ivImageTrans.setVisibility(View.GONE);
                        pbLoading.setVisibility(View.GONE);
                        return false;
                    }
                }).into(ivImageTrans);
            }
        }
    }

    public static void start(Activity activity, ShowPhoto showPhoto, View from)
    {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                new Pair<>(from,
                        activity.getString(R.string.transition_image_2))
        );
        ActivityCompat.startActivity(activity, new Intent(activity, PhotoViewActivity.class).putExtra(INTENT_EXTRA_PHOTO, showPhoto), options.toBundle());
    }
}
