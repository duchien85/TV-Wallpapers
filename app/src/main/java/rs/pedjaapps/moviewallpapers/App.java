package rs.pedjaapps.moviewallpapers;

import android.app.Application;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;

import org.skynetsoftware.dataloader.Cache;
import org.skynetsoftware.dataloader.MemCache;
import org.skynetsoftware.snet.AndroidInternet;
import org.skynetsoftware.snet.AndroidNetwork;
import org.skynetsoftware.snet.AndroidRequestManager;
import org.skynetsoftware.snet.AndroidTextManager;
import org.skynetsoftware.snet.AndroidUI;
import org.skynetsoftware.snet.Internet;
import org.skynetsoftware.snet.Network;
import org.skynetsoftware.snet.Request;
import org.skynetsoftware.snet.RequestManager;
import org.skynetsoftware.snet.SNet;
import org.skynetsoftware.snet.TextManager;
import org.skynetsoftware.snet.UI;

import java.io.File;

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

    public File DOWNLOAD_DIR;

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

        SNet.initialize(network, internet, ui, textManager);

        Request.setDefaultRequestUrl(API.REQUEST_URL);
        RequestManager.initialize(new AndroidRequestManager());
        RequestManager.getInstance().setGlobalRequestHandler(new MRequestHandler());
        SNet.LOGGING = SettingsManager.DEBUG();

        initDownloadDir();
    }

    private void initDownloadDir()
    {
        File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        DOWNLOAD_DIR = new File(pictures, "tvwp");
        if(!DOWNLOAD_DIR.exists())
        {
            DOWNLOAD_DIR.mkdir();
        }
        if(!DOWNLOAD_DIR.exists())
            throw new IllegalStateException("Failed to create download dir on sdcard");
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        Cache.getInstance(MemCache.class).flushCache();
        Glide.get(this).clearMemory();
    }
}
