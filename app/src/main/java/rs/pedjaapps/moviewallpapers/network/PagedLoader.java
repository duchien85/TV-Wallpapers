package rs.pedjaapps.moviewallpapers.network;


import org.skynetsoftware.dataloader.AndroidDataLoader;
import org.skynetsoftware.dataloader.CacheDataProvider;
import org.skynetsoftware.dataloader.DataLoader;
import org.skynetsoftware.dataloader.DataProvider;
import org.skynetsoftware.dataloader.MemCache;
import org.skynetsoftware.dataloader.SimpleLoadListener;
import org.skynetsoftware.snet.Request;

import java.util.ArrayList;
import java.util.List;

import rs.pedjaapps.moviewallpapers.model.Page;
import rs.pedjaapps.moviewallpapers.model.TypeListItem;

/**
 * Created by pedja on 29.10.14. 11.22.
 * This class is part of the Tulfie
 * Copyright © 2014 ${OWNER}
 *
 * Handles loading data page-by-page
 */
public class PagedLoader<T extends TypeListItem.IListItem>
{
    public static final int PROVIDERS_ADD_AFTER = 0;
    public static final int PROVIDERS_ADD_BEFORE = 1;
    /**
     * List holding all loaded items*/
    private List<TypeListItem<T>> items;
    private int page = 0, count = 0;
    private boolean next = true;

    /**
     * callback for loading*/
    private LoadListener<T> loadListener;

    /**
     * Request code for TSRequestManager*/
    private int requestCode;

    /**
     * optional parameters for TSRequestManager*/
    private String[] optParams;

    private DataLoader<Page<T>> dataLoader;
    private Request request;
    private DatabaseDataProvider<Page<T>> databaseDataProvider;
    private CacheDataProvider<MemCache, Page<T>> memoryCacheDataProvider;
    private List<DataProvider<Page<T>>> additionalProviders;
    private int additionalProvidersAdd = PROVIDERS_ADD_AFTER;

    public PagedLoader(int requestCode, Request request, DatabaseDataProvider.Query query, String... optParams)
    {
        this(requestCode, true, request, query, optParams);
    }

    public PagedLoader(int requestCode, Request request, DatabaseDataProvider.Query query, boolean memcache, String... optParams)
    {
        this(requestCode, true, request, query, memcache, optParams);
    }

    public PagedLoader(final int requestCode, final boolean autoAddLoaderItem, final Request request, DatabaseDataProvider.Query query, String... optParams)
    {
        this(requestCode, autoAddLoaderItem, request, query, true, optParams);
    }

    public PagedLoader(final int requestCode, final boolean autoAddLoaderItem, final Request request, DatabaseDataProvider.Query query, boolean memcache, String... optParams)
    {
        this.optParams = optParams;
        this.requestCode = requestCode;
        this.request = request;
        items = new ArrayList<>();

        databaseDataProvider = new DatabaseDataProvider<>(requestCode, query);
        if(request != null && memcache)memoryCacheDataProvider = new CacheDataProvider<>(request.getRequestUrl(), MemCache.class);

        dataLoader = new AndroidDataLoader<>();
        dataLoader.setListener(new SimpleLoadListener<Page<T>>()
        {
            @Override
            public void onLoadingFinished(int status)
            {
                if (loadListener != null) loadListener.onLoadingFinished();
            }

            @Override
            public void onDataLoaded(DataLoader.Result<Page<T>> result)
            {
                if (result.status == DataLoader.Result.STATUS_OK && result.data != null)
                {
                    boolean isMem = (result.provider instanceof CacheDataProvider);
                    Page<T> pge = result.data;
                    int oldPage = page;
                    if (!isMem)//only if response is no from memcache set page,count,next
                    {
                        page = pge.page;
                        count = pge.count;
                        next = pge.next;
                    }

                    //if last item is 'loader', remove it
                    if ((autoAddLoaderItem || oldPage == page) && !items.isEmpty() && items.get(items.size() - 1).getViewType() == TypeListItem.VIEW_TYPE_LOADER)
                    {
                        items.remove(items.size() - 1);
                    }
                    //if we just got same page like previous page, just return
                    if(oldPage == page && !(result.provider instanceof CacheDataProvider))
                    {
                        if (loadListener != null) loadListener.onLoaded(true, null, result.provider);
                        return;
                    }
                    List<TypeListItem<T>> list = pge.items;

                    ///add loader item if has next and enabled
                    if (autoAddLoaderItem && next && !isMem)
                    {
                        list.add(new TypeListItem<T>());
                    }
                    ///if we got result from network, and its page 1, add it to memcache
                    if(pge.page == 1 && result.provider instanceof NetworkDataProvider)
                    {
                        MemCache.getInstance(MemCache.class).addToCache(request.getRequestUrl(), pge);
                        items.clear();
                    }
                    list.removeAll(items);
                    items.addAll(list);
                    if (loadListener != null) loadListener.onLoaded(true, list, result.provider);
                }
                else
                {
                    //remove 'loader' item and return error
                    if (!items.isEmpty() && items.get(items.size() - 1).getViewType() == TypeListItem.VIEW_TYPE_LOADER)
                    {
                        items.remove(items.size() - 1);
                    }
                    if (loadListener != null) loadListener.onLoaded(false, null, null);
                }
            }
        });
    }

    /**
     * Get count of real items in list, excluding 'loader' items*/
    public static <T extends TypeListItem.IListItem> int getItemsCount(List<TypeListItem<T>> list)
    {
        int count = 0;
        for(TypeListItem<T> item : list)
        {
            if(item.getViewType() != TypeListItem.VIEW_TYPE_LOADER)
                count++;
        }
        return count;
    }

    /**
     * Load next page
     * This will cancel all active tasks with same id*/
    public void loadNextPage()
    {
        if(!next)return;
        dataLoader.cancel();
        request.addParam("page", String.valueOf(page + 1),true);
        List<DataProvider<Page<T>>> providers = new ArrayList<>();
        NetworkDataProvider<Page<T>> networkDataProvider = new NetworkDataProvider<>(request, requestCode, page == 0, optParams);
        if(page == 0 && memoryCacheDataProvider != null)
        {
            providers.add(memoryCacheDataProvider);
        }
        if(additionalProviders != null && additionalProvidersAdd == PROVIDERS_ADD_BEFORE)
            providers.addAll(additionalProviders);
        providers.add(networkDataProvider);
        providers.add(databaseDataProvider);
        if(additionalProviders != null && additionalProvidersAdd == PROVIDERS_ADD_AFTER)
            providers.addAll(additionalProviders);
        dataLoader.setProviders(providers);
        dataLoader.loadData();
    }

    /**
     * Resets loader
     * to initial state(for loading first page)*/
    public void reset()
    {
        dataLoader.cancel();
        page = 0;
        next = true;
        count = 0;
        items.clear();
        loadNextPage();
    }

    public List<TypeListItem<T>> getItems()
    {
        return items;
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public void setLoadListener(LoadListener<T> listener)
    {
        this.loadListener = listener;
    }

    public int getPage()
    {
        return page;
    }

    public int getCount()
    {
        return count;
    }

    public void setOptParams(String... optParams)
    {
        this.optParams = optParams;
    }

    public void setRequest(Request request)
    {
        this.request = request;
    }

    public void setQuery(DatabaseDataProvider.Query query)
    {
        this.databaseDataProvider.setQuery(query);
    }

    @Deprecated
    public interface OnLoadListener<T extends TypeListItem.IListItem>
    {
        /**
         * @param success if loading was successful*/
        void onLoaded(boolean success, List<TypeListItem<T>> newItems);
        /**
         * Called when loading has been finished or error occurred*/
        void onLoadingFinished();
    }

    public interface LoadListener<T extends TypeListItem.IListItem>
    {
        /**
         * @param success if loading was successful*/
        void onLoaded(boolean success, List<TypeListItem<T>> newItems, DataProvider<Page<T>> dataProvider);
        /**
         * Called when loading has been finished or error occurred*/
        void onLoadingFinished();
    }

    /**
     * Cancel the request*/
    public void cancel()
    {
        if(dataLoader != null)
            dataLoader.cancel();
    }

    public Request getRequest()
    {
        return request;
    }

    /**
     * Set additional providers that will be added to each request, either before main provider or after
     * @param additionalProvidersAdd where to add providers, one of {@link #PROVIDERS_ADD_AFTER} or {@link #PROVIDERS_ADD_BEFORE}*/
    public void setAdditionalProviders(List<DataProvider<Page<T>>> additionalProviders, int additionalProvidersAdd)
    {
        this.additionalProviders = additionalProviders;
        this.additionalProvidersAdd = additionalProvidersAdd;
    }
}
