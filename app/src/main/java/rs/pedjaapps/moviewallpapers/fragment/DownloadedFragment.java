package rs.pedjaapps.moviewallpapers.fragment;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.skynetsoftware.jutils.RVArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import rs.pedjaapps.moviewallpapers.PhotoViewActivity;
import rs.pedjaapps.moviewallpapers.R;
import rs.pedjaapps.moviewallpapers.adapter.GridAdapter;
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
public class DownloadedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, RVArrayAdapter.OnItemClickListener
{
    private GridAdapter mAdapter;
    private ProgressBar pbLoading;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.photo_grid_fragment, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        tvEmptyView = (TextView) view.findViewById(R.id.tvEmptyView);

        final GridLayoutManager glm = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.grid_column_count));
        recyclerView.setLayoutManager(glm);

        mAdapter = new GridAdapter(getActivity(), new ArrayList<TypeListItem<ShowPhoto>>(), null, getString(R.string.transition_image_1), true, false);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(this);

        onRefresh();

        getActivity().registerReceiver(downloadCompletedReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        return view;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        getActivity().unregisterReceiver(downloadCompletedReceiver);
    }


    @Override
    public void onRefresh()
    {
        List<ShowPhoto> photos = DownloadUtils.listDownloadePhotos();
        mAdapter.clear();
        for (ShowPhoto photo : photos)
        {
            mAdapter.add(new TypeListItem<>(photo));
        }
        tvEmptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        pbLoading.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onItemClick(View view, Object item, int position)
    {
        PhotoViewActivity.start(getActivity(), (ShowPhoto) item, view);
    }

    private BroadcastReceiver downloadCompletedReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            onRefresh();
        }
    };
}
