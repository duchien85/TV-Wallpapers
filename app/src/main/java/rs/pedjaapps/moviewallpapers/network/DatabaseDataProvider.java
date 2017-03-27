package rs.pedjaapps.moviewallpapers.network;

import android.os.Parcel;
import android.os.Parcelable;

import org.skynetsoftware.dataloader.DataProvider;


/**
 * Created by pedja on 23.9.15. 13.28.
 * This class is part of the Tulfie
 * Copyright © 2015 ${OWNER}
 *
 * {@link DataProvider} that loads data from local database
 */
public class DatabaseDataProvider<T> implements DataProvider<T>
{
    private int requestCode;
    private T resultData;
    private Query query;

    public DatabaseDataProvider(int requestCode)
    {
        this.requestCode = requestCode;
    }

    public DatabaseDataProvider(int requestCode, Query query)
    {
        this.requestCode = requestCode;
        this.query = query;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean load()
    {
        switch (requestCode)
        {
            /*case NetworkDataProvider.REQUEST_CODE_LAST_20_PHOTOS:
                String q = "SELECT * FROM photo WHERE member_id = ? GROUP BY photo._id ORDER BY created DESC LIMIT 19";
                String[] args = new String[]{User.get().id + ""};
                List<Photo> last19 = DatabaseManager.getInstance().getPhotos(q, args);

                q = "SELECT * FROM photo WHERE member_id = ? AND is_default = 1 LIMIT 1";
                List<Photo> defPhoto = DatabaseManager.getInstance().getPhotos(q, args);
                last19.addAll(defPhoto);
                resultData = (T) last19;
                return !last19.isEmpty();*/

        }
        return false;
    }

    public void setQuery(Query query)
    {
        this.query = query;
    }

    @Override
    public T getResult()
    {
        return resultData;
    }

    @Override
    public boolean forceLoading()
    {
        return false;
    }

    @Override
    public Object getMetadata()
    {
        return null;
    }

    public static class Query implements Parcelable
    {
        public String query;
        public String[] args;

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            dest.writeString(this.query);
            dest.writeStringArray(this.args);
        }

        public Query()
        {
        }

        protected Query(Parcel in)
        {
            this.query = in.readString();
            this.args = in.createStringArray();
        }

        public static final Creator<Query> CREATOR = new Creator<Query>()
        {
            public Query createFromParcel(Parcel source)
            {
                return new Query(source);
            }

            public Query[] newArray(int size)
            {
                return new Query[size];
            }
        };
    }
}
