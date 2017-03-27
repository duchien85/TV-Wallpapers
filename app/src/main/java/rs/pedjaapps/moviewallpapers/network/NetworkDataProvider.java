package rs.pedjaapps.moviewallpapers.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.skynetsoftware.dataloader.DataProvider;
import org.skynetsoftware.snet.Request;
import org.skynetsoftware.snet.ResponseParser;
import org.skynetsoftware.snet.SNet;

import rs.pedjaapps.moviewallpapers.BuildConfig;
import rs.pedjaapps.moviewallpapers.model.JSONParser;

public class NetworkDataProvider<T> implements DataProvider<T>
{
    public static final int REQUEST_CODE_SHOWS_PHOTOS = 1;
    public static final int REQUEST_CODE_SHOWS_AS_PHOTOS = 2;
    public static final int REQUEST_CODE_SHOW = 3;

    private Request request;
    private T resultData;
    private int requestCode;
    private Object[] optParams;
    private boolean forceLoading = true;
    private Handler uiHandler;

    public NetworkDataProvider(Request request, int requestCode, Object... optParams)
    {
        this(request, requestCode, true, optParams);
    }

    public NetworkDataProvider(Request request, int requestCode, boolean forceLoading, Object... optParams)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (requestCode <= 0)
        {
            throw new IllegalArgumentException("invalid request code");
        }
        this.forceLoading = forceLoading;
        this.optParams = optParams;
        this.request = request;
        this.requestCode = requestCode;
        uiHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public boolean load()
    {
        if (!SNet.getInstance().getNetwork().isNetworkAvailable())
        {
            return false;
        }
        else
        {
            if (BuildConfig.DEBUG)
                Log.d("network-data-provider", String.format("NetworkDataProvider::load()[requestCode=%s]", requestCode));
            final JSONParser jsonParser = new JSONParser(SNet.getInstance().getInternet().executeHttpRequest(request));
            switch (requestCode)
            {
                case REQUEST_CODE_SHOWS_PHOTOS:
                    jsonParser.parseGetShowPhotosResponse();
                    break;
                case REQUEST_CODE_SHOWS_AS_PHOTOS:
                    jsonParser.parseGetShowsAsPhotosResponse();
                    break;
                case REQUEST_CODE_SHOW:
                    jsonParser.parseGetShowResponse();
                    break;
            }
            return handleResponse(jsonParser);
        }
    }

    private boolean handleResponse(final JSONParser jsonParser)
    {
        if (jsonParser.getResponseStatus() == ResponseParser.RESPONSE_STATUS_SUCCESS)
        {
            resultData = jsonParser.getParseObject();
            return true;
        }
        else
        {
            uiHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (jsonParser.getResponseMessage() != null)
                    {
                        SNet.getInstance().getUi().showToast(jsonParser.getResponseMessage());
                    }
                    else if (!jsonParser.getServerResponse().isResponseOk())
                    {
                        SNet.getInstance().getUi().showToast(SNet.getInstance().getTextManager().getText("unknown_error"));
                    }
                }
            });
            return false;
        }
    }

    @Override
    public T getResult()
    {
        return resultData;
    }

    @Override
    public boolean forceLoading()
    {
        return forceLoading;
    }

    @Override
    public Object getMetadata()
    {
        return null;
    }
}