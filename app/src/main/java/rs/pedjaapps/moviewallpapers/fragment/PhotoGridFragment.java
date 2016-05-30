package rs.pedjaapps.moviewallpapers.fragment;

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

import com.androidforever.dataloader.DataProvider;
import com.tehnicomsolutions.http.Request;

import java.util.List;

import rs.pedjaapps.moviewallpapers.R;
import rs.pedjaapps.moviewallpapers.adapter.GridAdapter;
import rs.pedjaapps.moviewallpapers.model.Page;
import rs.pedjaapps.moviewallpapers.model.ShowPhoto;
import rs.pedjaapps.moviewallpapers.model.TypeListItem;
import rs.pedjaapps.moviewallpapers.network.NetworkDataProvider;
import rs.pedjaapps.moviewallpapers.network.PagedLoader;

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
public abstract class PhotoGridFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, PagedLoader.LoadListener<ShowPhoto>,GridAdapter.LoadMorePhotos
{
    private GridAdapter mAdapter;
    private ProgressBar pbLoading;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PagedLoader<ShowPhoto> pagedLoader;
    private TextView tvEmptyView;

    private boolean loading = false;
    private int previousTotal = 0;
    private int firstVisibleItem, visibleItemCount, totalItemCount;
    private boolean wantToLoadMore;

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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            private boolean canLoadMore = false;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dx == 0 && dy == 0) return;
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = glm.getItemCount();
                firstVisibleItem = glm.findFirstVisibleItemPosition();

                if (!canLoadMore)
                {
                    if (totalItemCount > previousTotal)
                    {
                        canLoadMore = true;
                        previousTotal = totalItemCount;
                    }
                }
                if (canLoadMore && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 9))
                {
                    loadMore();
                    canLoadMore = false;
                }
            }
        });

        Request request = getRequest();

        pagedLoader = new PagedLoader<>(NetworkDataProvider.REQUEST_CODE_SHOWS_PHOTOS, request, null);
        pagedLoader.setLoadListener(this);

        mAdapter = new GridAdapter(getActivity(), pagedLoader.getItems(), this);
        recyclerView.setAdapter(mAdapter);

        onRefresh();

        return view;
    }

    protected abstract Request getRequest();

    @Override
    public void loadMore()
    {
        if (loading)
        {
            wantToLoadMore = true;
            return;
        }
        pagedLoader.loadNextPage();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        pagedLoader.cancel();
        pagedLoader.setLoadListener(null);
        pagedLoader = null;
    }

    @Override
    public void onRefresh()
    {
        loading = false;
        previousTotal = 0;
        firstVisibleItem = 0;
        visibleItemCount = 0;
        totalItemCount = 0;
        pagedLoader.setAdditionalProviders(null, 0);
        pagedLoader.reset();
        loading = true;
    }

    @Override
    public void onLoaded(boolean success, List<TypeListItem<ShowPhoto>> newItems, DataProvider<Page<ShowPhoto>> dataProvider)
    {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadingFinished()
    {
        tvEmptyView.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        pbLoading.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        loading = false;
        if(wantToLoadMore)
        {
            loadMore();
            wantToLoadMore = false;
        }
    }
}
