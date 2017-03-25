package rs.pedjaapps.moviewallpapers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.af.jutils.RVArrayAdapter;
import com.androidforever.dataloader.AndroidDataLoader;
import com.androidforever.dataloader.DataLoader;
import com.androidforever.dataloader.DataProvider;
import com.androidforever.dataloader.MemoryCacheDataProvider;
import com.tehnicomsolutions.http.Request;

import java.util.ArrayList;
import java.util.List;

import rs.pedjaapps.moviewallpapers.adapter.GridAdapter;
import rs.pedjaapps.moviewallpapers.model.Show;
import rs.pedjaapps.moviewallpapers.model.ShowPhoto;
import rs.pedjaapps.moviewallpapers.model.TypeListItem;
import rs.pedjaapps.moviewallpapers.network.NetworkDataProvider;
import rs.pedjaapps.moviewallpapers.utility.ImageUtility;

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
public class ShowActivity extends AppCompatActivity implements DataLoader.LoadListener<Show>, RVArrayAdapter.OnItemClickListener
{
    public static final String INTENT_EXTRA_SHOW_ID = "show_id";
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ProgressBar pbLoading;
    private GridAdapter mAdapter;
    private ImageView ivHeader;

    private Show mShow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        long showId = getIntent().getLongExtra(INTENT_EXTRA_SHOW_ID, -1);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.ctl);

        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);

        ivHeader = (ImageView) findViewById(R.id.ivImage);

        RecyclerView rvPhotos = (RecyclerView) findViewById(R.id.rvPhotos);

        final GridLayoutManager glm = new GridLayoutManager(this, getResources().getInteger(R.integer.grid_column_count));
        rvPhotos.setLayoutManager(glm);

        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {
            @Override
            public int getSpanSize(int position)
            {
                switch (mAdapter.getItemViewType(position))
                {
                    case ShowPhoto.VIEW_TYPE_OVERVIEW:
                        return getResources().getInteger(R.integer.grid_column_count);
                }
                return 1;
            }
        });

        mAdapter = new GridAdapter(this, new ArrayList<TypeListItem<ShowPhoto>>(), null, getString(R.string.transition_image_2), false, true);
        rvPhotos.setAdapter(mAdapter);

        DataLoader<Show> dataLoader = new AndroidDataLoader<>();
        List<DataProvider<Show>> dataProviders = new ArrayList<>();

        Request request = new Request(Request.Method.GET);
        request.addUrlPart("shows").addUrlPart(String.valueOf(showId));
        request.addParam("with_photo", true);
        request.addParam("with_photos", true);

        NetworkDataProvider<Show> networkDataProvider = new NetworkDataProvider<>(request, NetworkDataProvider.REQUEST_CODE_SHOW);
        MemoryCacheDataProvider<Show> memoryCacheDataProvider = new MemoryCacheDataProvider<>(request.getRequestUrl());

        dataProviders.add(memoryCacheDataProvider);
        dataProviders.add(networkDataProvider);

        dataLoader.setProviders(dataProviders);
        dataLoader.setListener(this);

        dataLoader.loadData();

        mAdapter.setOnItemClickListener(this);
    }


    @Override
    public void onLoadingFinished(int status)
    {
        pbLoading.setVisibility(View.GONE);
    }

    @Override
    public void onDataLoaded(DataLoader.Result<Show> result)
    {
        if (result.data != null)
        {
            mShow = result.data;
            mCollapsingToolbarLayout.setTitle(String.format("%s (%d)", mShow.title, mShow.year));
            mAdapter.addAll(result.data.photos, true);

            App.get().getGlobalImageLoader().get(ImageUtility.generateImageUrlThumb(mShow.showPhoto), ivHeader);
            App.get().getGlobalImageLoader().get(ImageUtility.generateImageUrlFull(mShow.showPhoto), ivHeader);
        }
    }

    @Override
    public void onLoadStarted()
    {
        pbLoading.setVisibility(View.VISIBLE);
    }

    public static void start(Activity activity, long showId, View from)
    {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                new Pair<>(from,
                        activity.getString(R.string.transition_image_1))
        );
        ActivityCompat.startActivity(activity, new Intent(activity, ShowActivity.class).putExtra(INTENT_EXTRA_SHOW_ID, showId), options.toBundle());
    }

    @Override
    public void onItemClick(View view, Object item, int position)
    {
        PhotoViewActivity.start(this, (ShowPhoto) item, view);
    }
}
