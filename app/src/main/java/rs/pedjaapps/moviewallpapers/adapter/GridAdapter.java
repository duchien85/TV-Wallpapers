package rs.pedjaapps.moviewallpapers.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.skynetsoftware.jutils.Android;
import org.skynetsoftware.jutils.DisplayUtils;
import org.skynetsoftware.jutils.RVArrayAdapter;

import java.util.List;

import rs.pedjaapps.moviewallpapers.App;
import rs.pedjaapps.moviewallpapers.R;
import rs.pedjaapps.moviewallpapers.model.ShowPhoto;
import rs.pedjaapps.moviewallpapers.model.TypeListItem;
import rs.pedjaapps.moviewallpapers.utility.DownloadUtils;
import rs.pedjaapps.moviewallpapers.utility.ImageUtility;

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
public class GridAdapter extends RVArrayAdapter<TypeListItem<ShowPhoto>>
{
    private int screenWidth, columnCount;
    private LoadMorePhotos loadMorePhotos;
    private int lastPosition = -1;
    private String transitionName;
    private boolean displayShow, showDownload;

    public GridAdapter(@NonNull Context context, @NonNull List<TypeListItem<ShowPhoto>> items,
                       LoadMorePhotos loadMorePhotos, String transitionName, boolean displayShow, boolean showDownload)
    {
        super(context, items);
        this.loadMorePhotos = loadMorePhotos;
        screenWidth = new DisplayUtils(context).screenWidth;
        columnCount = context.getResources().getInteger(R.integer.grid_column_count);
        this.transitionName = transitionName;
        this.displayShow = displayShow;
        this.showDownload = showDownload;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if (viewType == ShowPhoto.VIEW_TYPE_PHOTO)
        {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.photo_grid_item, parent, false));
        }
        else if (viewType == ShowPhoto.VIEW_TYPE_OVERVIEW)
        {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.photo_grid_overview, parent, false));
        }
        else if (viewType == TypeListItem.VIEW_TYPE_LOADER)
        {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.loader_grid_item, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position)
    {
        ViewHolder holder = (ViewHolder) viewHolder;
        final ShowPhoto photo = getItem(position).getItem();

        int viewType = getItemViewType(position);
        if (viewType == ShowPhoto.VIEW_TYPE_PHOTO)
        {
            if (Android.hasLollipop())
            {
                holder.ivImage.setTransitionName(transitionName);
            }

            holder.llShow.setVisibility(displayShow ? View.VISIBLE : View.GONE);
            holder.ivDownload.setVisibility(showDownload && !photo.downloading ? View.VISIBLE : View.GONE);

            if (photo.show != null)
            {
                holder.tvTitle.setText(photo.show.title);
                if(photo.show.year <= 0)
                {
                    holder.tvYear.setVisibility(View.GONE);
                }
                else
                {
                    holder.tvYear.setText(String.valueOf(photo.show.year));
                    holder.tvYear.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                holder.tvTitle.setText(null);
                holder.tvYear.setText(null);
            }
            if(photo.fullPath)
            {
                Glide.with(context).load(photo.filename).dontAnimate().into(holder.ivImage);
            }
            else
            {
                Glide.with(context).load(ImageUtility.generateImageUrlThumb(photo)).dontAnimate().into(holder.ivImage);
            }
            setAnimation(holder.itemView, position);

            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClick(view.findViewById(R.id.ivImage), photo, position);
                }
            });

            holder.ivDownload.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(!DownloadUtils.isPhotoDownloaded(photo))
                    {
                        DownloadUtils.download(photo);
                        photo.downloading = true;
                        notifyDataSetChanged();
                    }
                }
            });

            setDimensions(holder);
        }
        else if (viewType == ShowPhoto.VIEW_TYPE_OVERVIEW)
        {
            holder.tvOverview.setText(photo.show.overview);
            holder.tvYear.setText(String.valueOf(photo.show.year));
        }
        else if (viewType == TypeListItem.VIEW_TYPE_LOADER)
        {
            if (loadMorePhotos != null)
                loadMorePhotos.loadMore();
            setDimensions(holder);
        }
    }

    private void setDimensions(ViewHolder holder)
    {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        params.width = screenWidth / columnCount;
        //noinspection SuspiciousNameCombination
        params.height = params.width;
        holder.itemView.setLayoutParams(params);
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            //Animation animation = AnimationUtils.loadAnimation(context, position % 2 == 0 ? R.anim.slide_in_left : R.anim.slide_in_right);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_up);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(final RecyclerView.ViewHolder holder)
    {
        ((ViewHolder) holder).clearAnimation();
    }

    @Override
    public int getItemViewType(int position)
    {
        return getItem(position).getViewType();
    }

    public interface LoadMorePhotos
    {
        void loadMore();
    }

    private class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView ivImage, ivDownload;
        private TextView tvTitle, tvYear, tvOverview;
        private LinearLayout llShow;

        public ViewHolder(View itemView)
        {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            ivDownload = (ImageView) itemView.findViewById(R.id.ivDownload);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvYear = (TextView) itemView.findViewById(R.id.tvYear);
            tvOverview = (TextView) itemView.findViewById(R.id.tvOverview);
            llShow = (LinearLayout) itemView.findViewById(R.id.llShow);
        }

        public void clearAnimation()
        {
            itemView.clearAnimation();
        }
    }
}
