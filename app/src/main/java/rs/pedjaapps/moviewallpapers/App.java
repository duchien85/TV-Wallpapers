package rs.pedjaapps.moviewallpapers;

import android.app.Activity;
import android.app.Application;

import com.android.volley.VolleyLog;
import com.android.volley.cache.DiskLruBasedCache;
import com.android.volley.cache.plus.SimpleImageLoader;
import com.androidforever.dataloader.MemCache;
import com.crashlytics.android.Crashlytics;
import com.tehnicomsolutions.http.AndroidInternet;
import com.tehnicomsolutions.http.AndroidNetwork;
import com.tehnicomsolutions.http.AndroidRequestManager;
import com.tehnicomsolutions.http.AndroidTextManager;
import com.tehnicomsolutions.http.AndroidUI;
import com.tehnicomsolutions.http.Http;
import com.tehnicomsolutions.http.Internet;
import com.tehnicomsolutions.http.Network;
import com.tehnicomsolutions.http.Request;
import com.tehnicomsolutions.http.RequestManager;
import com.tehnicomsolutions.http.TextManager;
import com.tehnicomsolutions.http.UI;

import io.fabric.sdk.android.Fabric;
import rs.pedjaapps.moviewallpapers.network.MRequestHandler;
import rs.pedjaapps.moviewallpapers.utility.SettingsManager;

/**
 * Created by pedja on 10/8/13 10.15.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 * <p/>
 * Main Application class
 * Created when application is first started
 * It stays in memory as long as app is alive
 *
 * @author Predrag Čokulov
 */
public class App extends Application
{
    private static App app = null;

    private SimpleImageLoader mGlobalImageLoader;
    private DiskLruBasedCache.ImageCacheParams cacheParams;
    private Activity mCurrentActivity = null;

    public static App get()
    {
        return app;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        //LeakCanary.install(this);
        try
        {
            Fabric.with(this, new Crashlytics());//setup crashlytics
        }
        catch (Exception ignore)
        {
        }
        app = this;

        Network network = new AndroidNetwork(this);
        Internet internet = new AndroidInternet(this);
        UI ui = new AndroidUI(this);
        TextManager textManager = new AndroidTextManager(this);

        Http.initialize(network, internet, ui, textManager);

        Request.setDefaultRequestUrl(API.REQUEST_URL);
        RequestManager.initialize(new AndroidRequestManager());
        RequestManager.getInstance().setGlobalRequestHandler(new MRequestHandler());
        Http.LOGGING = SettingsManager.DEBUG();

        initImageLoader();
    }

    /**Init image loader cache*/
    public void initImageLoader()
    {
        cacheParams = new DiskLruBasedCache.ImageCacheParams(this, ".cache");
        cacheParams.setMemCacheSizePercent(0.1f);
        cacheParams.diskCacheSize = 1024 * 1024 * 200;//200MB, is it ot much?
        cacheParams.diskCacheEnabled = true;
        cacheParams.memoryCacheEnabled = false;//this does nothing o:O
        VolleyLog.DEBUG = SettingsManager.DEBUG();

        mGlobalImageLoader = new SimpleImageLoader(this, cacheParams);
    }

    public Activity getCurrentActivity()
    {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity currentActivity)
    {
        this.mCurrentActivity = currentActivity;
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        MemCache.getInstance().flushCache();
        mGlobalImageLoader.flushCache();
    }

    public DiskLruBasedCache.ImageCacheParams getCacheParams()
    {
        return cacheParams;
    }

    public SimpleImageLoader getGlobalImageLoader()
    {
        return mGlobalImageLoader;
    }
}
