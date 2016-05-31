package rs.pedjaapps.moviewallpapers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import rs.pedjaapps.moviewallpapers.model.Show;
import rs.pedjaapps.moviewallpapers.model.ShowPhoto;

/**
 * Created by pedja on 12.5.16. 16.04.
 * This class is part of the sketcher
 * Copyright © 2016 ${OWNER}
 */
public class DatabaseManager
{
    private final static Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    private static DatabaseManager instance;

    public static void init() throws IOException
    {
        try
        {
            Class.forName(JDBC_DRIVER);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        if (instance != null)
            throw new IllegalStateException("DatabaseManager already initialized");
        instance = new DatabaseManager();
    }

    public static DatabaseManager getInstance()
    {
        if (instance == null)
            throw new IllegalStateException("You must initialize 'DatabaseManager' before use");
        return instance;
    }

    private Random random = new Random();
    private Connection conn = null;
    private boolean debug = true;

    public DatabaseManager() throws IOException
    {
        LOGGER.setLevel(Level.INFO);
        //LOGGER.addHandler(new FileHandler());
        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://pedjaapps.net:3306/moviewallpapers?useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false&autoReconnect=true", "mwp", "vN@q55j5");
        }
        catch (Exception e)
        {
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Connection getConn()
    {
        return conn;
    }

    public int insertShow(Show show)
    {
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO shows (id, title, imdb_id, year, overview) VALUES(?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, show.id);
            preparedStatement.setString(2, show.title);
            preparedStatement.setString(3, show.imdb);
            preparedStatement.setInt(4, show.year);
            preparedStatement.setString(5, show.overview);
            return preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return -1;
        }
    }

    public int insertInvalidId(int id)
    {
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT IGNORE INTO invalid_ids (id) VALUES(?)");
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return -1;
        }
    }

    public int insertPhotoInvalidId(int id)
    {
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT IGNORE INTO photo_invalid_ids (id) VALUES(?)");
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return -1;
        }
    }

    public int insertShowPhoto(ShowPhoto showPhoto)
    {
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT IGNORE INTO show_photo (show_id, filename, type) VALUES(?, ?, ?)");
            preparedStatement.setInt(1, showPhoto.showId);
            preparedStatement.setString(2, showPhoto.filename);
            preparedStatement.setString(3, showPhoto.type);
            return preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return -1;
        }
    }

    public int insertPopularShows(List<Integer> ids)
    {
        try
        {
            conn.setAutoCommit(false);
            int count = 0;
            int offset = 0;
            for (Integer id : ids)
            {
                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO popular_shows (show_id, ord) VALUES(?, ?)");
                preparedStatement.setInt(1, id);
                preparedStatement.setInt(2, offset);
                count += preparedStatement.executeUpdate();
                offset++;
            }
            conn.commit();
            return count;
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return 0;
        }
    }


    public Integer[] getShowIds()
    {
        try
        {
            PreparedStatement countStatement = conn.prepareStatement("SELECT COUNT(*) AS total FROM shows");
            ResultSet countResultSet = countStatement.executeQuery();
            if (!countResultSet.next())
                return new Integer[0];

            Integer[] ids = new Integer[countResultSet.getInt("total")];

            PreparedStatement selectStatement = conn.prepareStatement("SELECT id FROM shows ORDER BY id ASC");

            ResultSet resultSet = selectStatement.executeQuery();

            int i = 0;
            while (resultSet.next())
            {
                ids[i] = resultSet.getInt("id");
                i++;
            }

            return ids;
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return new Integer[0];
        }
    }

    public Integer[] getShowIdsFromPhotoShows()
    {
        List<Integer> ids = new ArrayList<Integer>();
        try
        {
            PreparedStatement selectStatement = conn.prepareStatement("SELECT show_id FROM show_photo GROUP BY show_id ORDER BY show_id ASC");

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                ids.add(resultSet.getInt("show_id"));
            }

            return ids.toArray(new Integer[ids.size()]);
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return new Integer[0];
        }
    }

    public List<Show> getShows(int page, int perPage, boolean getOverview, boolean withPoster)
    {
        List<Show> shows = new ArrayList<>(perPage + 1);
        try
        {
            int offset = (page - 1) * perPage;
            PreparedStatement selectStatement = conn.prepareStatement("SELECT " + (withPoster ? "sp.show_id, sp.filename, sp.type, " : "") + " s.id, s.title, s.imdb_id, s.year" + (getOverview ? ", s.overview" : "") + " FROM shows s INNER JOIN show_photo sp ON s.id = sp.show_id WHERE s.title IS NOT NUll AND s.title != 'null' && s.title != '' ORDER BY s.year DESC LIMIT ?, ?");
            selectStatement.setInt(1, offset);
            selectStatement.setInt(2, perPage + 1);

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                Show show = new Show();
                show.id = resultSet.getInt("id");
                show.title = resultSet.getString("title");
                show.imdb = resultSet.getString("imdb_id");
                show.year = resultSet.getInt("year");
                if (getOverview) show.overview = resultSet.getString("overview");
                int sid = !withPoster ? -1 : resultSet.getInt("show_id");
                if(sid > 0)
                {
                    ShowPhoto showPhoto = new ShowPhoto();
                    showPhoto.showId = sid;
                    showPhoto.type = resultSet.getString("type");
                    showPhoto.filename = resultSet.getString("filename");
                    show.showPhoto = showPhoto;
                }
                shows.add(show);
            }

            return shows;
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return shows;
        }
    }

    public Show getShow(int showId, boolean withPoster)
    {
        try
        {
            PreparedStatement selectStatement;
            if (!withPoster)
            {
                selectStatement = conn.prepareStatement("SELECT id, title, imdb_id, year, overview FROM shows WHERE id = ? AND title IS NOT NUll AND title != 'null' && title != ''");
            }
            else
            {
                selectStatement = conn.prepareStatement("SELECT * FROM shows LEFT JOIN show_photo ON shows.id = show_photo.show_id AND show_photo.type = 'poster' WHERE shows.id = ? LIMIT 1");
            }
            selectStatement.setInt(1, showId);

            ResultSet resultSet = selectStatement.executeQuery();

            if(resultSet.next())
            {
                Show show = new Show();
                show.id = resultSet.getInt("id");
                show.title = resultSet.getString("title");
                show.imdb = resultSet.getString("imdb_id");
                show.year = resultSet.getInt("year");
                show.overview = resultSet.getString("overview");
                int sid = !withPoster ? -1 : resultSet.getInt("show_id");
                if(sid > 0)
                {
                    ShowPhoto showPhoto = new ShowPhoto();
                    showPhoto.showId = sid;
                    showPhoto.type = resultSet.getString("type");
                    showPhoto.filename = resultSet.getString("filename");
                    show.showPhoto = showPhoto;
                }
                return show;
            }
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
        }
        return null;
    }

    public List<ShowPhoto> getShowPhotos(int page, int perPage, boolean withShow)
    {
        List<ShowPhoto> shows = new ArrayList<>(perPage + 1);
        try
        {
            int offset = (page - 1) * perPage;
            PreparedStatement selectStatement;
            if (!withShow)
            {
                selectStatement = conn.prepareStatement("SELECT show_id, filename FROM show_photo WHERE type = ? ORDER BY modified DESC LIMIT ?, ?");
            }
            else
            {
                selectStatement = conn.prepareStatement("SELECT s.id, s.title, s.year, sp.show_id, sp.filename FROM show_photo sp INNER JOIN shows s ON s.id = sp.show_id WHERE sp.type = ? AND title IS NOT NUll AND title != 'null' && title != '' ORDER BY sp.modified DESC LIMIT ?, ?");
            }
            selectStatement.setString(1, "fanart");
            selectStatement.setInt(2, offset);
            selectStatement.setInt(3, perPage + 1);

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                ShowPhoto showPhoto = new ShowPhoto();
                showPhoto.showId = resultSet.getInt("show_id");
                showPhoto.filename = resultSet.getString("filename");

                int sid = !withShow ? -1 : resultSet.getInt("id");
                if(sid > 0)
                {
                    Show show = new Show();
                    show.id = sid;
                    show.title = resultSet.getString("title");
                    show.year = resultSet.getInt("year");
                    showPhoto.show = show;
                }

                shows.add(showPhoto);
            }

            return shows;
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return shows;
        }
    }



    public List<ShowPhoto> getShowPhotos(String showIds, boolean withShow)
    {
        List<ShowPhoto> shows = new ArrayList<>(10);
        try
        {
            PreparedStatement selectStatement;
            if (!withShow)
            {
                selectStatement = conn.prepareStatement("SELECT show_id, filename FROM show_photo WHERE type = ? AND show_id in (" + showIds + ") GROUP BY show_id ORDER BY modified DESC");
            }
            else
            {
                selectStatement = conn.prepareStatement("SELECT s.id, s.title, s.year, sp.show_id, sp.filename FROM show_photo sp INNER JOIN shows s ON s.id = sp.show_id WHERE sp.type = ? AND show_id in (" + showIds + ") AND title IS NOT NUll AND title != 'null' && title != '' GROUP BY sp.show_id ORDER BY sp.modified DESC");
            }
            selectStatement.setString(1, "fanart");

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                ShowPhoto showPhoto = new ShowPhoto();
                showPhoto.showId = resultSet.getInt("show_id");
                showPhoto.filename = resultSet.getString("filename");

                int sid = !withShow ? -1 : resultSet.getInt("id");
                if(sid > 0)
                {
                    Show show = new Show();
                    show.id = sid;
                    show.title = resultSet.getString("title");
                    show.year = resultSet.getInt("year");
                    showPhoto.show = show;
                }

                shows.add(showPhoto);
            }

            return shows;
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return shows;
        }
    }

    public List<ShowPhoto> getShowPhotos(int showId, int page, int perPage)
    {
        List<ShowPhoto> shows = new ArrayList<>(perPage + 1);
        try
        {
            int offset = (page - 1) * perPage;
            PreparedStatement selectStatement = conn.prepareStatement("SELECT show_id, filename FROM show_photo WHERE show_id = ? AND type = ? ORDER BY modified DESC LIMIT ?, ?");
            selectStatement.setInt(1, showId);
            selectStatement.setString(2, "fanart");
            selectStatement.setInt(3, offset);
            selectStatement.setInt(4, perPage + 1);

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                ShowPhoto showPhoto = new ShowPhoto();
                showPhoto.showId = resultSet.getInt("show_id");
                showPhoto.filename = resultSet.getString("filename");
                shows.add(showPhoto);
            }

            return shows;
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return shows;
        }
    }

    public List<Show> getPopularShows(boolean getOverview, boolean withPoster)
    {
        List<Show> shows = new ArrayList<>(100);
        try
        {
            PreparedStatement selectStatement;
            if (!withPoster)
            {
                selectStatement = conn.prepareStatement("SELECT id, title, imdb_id, year" + (getOverview ? ", overview" : "") + " FROM shows s INNER JOIN popular_shows ps ON s.id = ps.show_id WHERE s.title IS NOT NUll AND s.title != 'null' && s.title != '' ORDER BY ps.ord ASC");
            }
            else
            {
                selectStatement = conn.prepareStatement("SELECT sp.show_id, sp.filename, sp.type, s.id, s.title, s.imdb_id, s.year" + (getOverview ? ", s.overview" : "") + " FROM shows s INNER JOIN popular_shows ps ON s.id = ps.show_id LEFT JOIN show_photo sp ON s.id = sp.show_id WHERE s.title IS NOT NUll AND s.title != 'null' && s.title != '' GROUP BY s.id ORDER BY ps.ord ASC");
            }

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                Show show = new Show();
                show.id = resultSet.getInt("id");
                show.title = resultSet.getString("title");
                show.imdb = resultSet.getString("imdb_id");
                show.year = resultSet.getInt("year");
                if (getOverview) show.overview = resultSet.getString("overview");
                int sid = !withPoster ? -1 : resultSet.getInt("show_id");
                if(sid > 0)
                {
                    ShowPhoto showPhoto = new ShowPhoto();
                    showPhoto.showId = sid;
                    showPhoto.type = resultSet.getString("type");
                    showPhoto.filename = resultSet.getString("filename");
                    show.showPhoto = showPhoto;
                }
                shows.add(show);
            }

            return shows;
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return shows;
        }
    }

    public List<Show> getShows(String ids, boolean getOverview, boolean withPoster)
    {
        List<Show> shows = new ArrayList<>(10);
        try
        {
            PreparedStatement selectStatement;
            if (!withPoster)
            {
                selectStatement = conn.prepareStatement("SELECT id, title, imdb_id, year" + (getOverview ? ", overview" : "") + " FROM shows s WHERE id in (" + ids + ") AND s.title IS NOT NUll AND s.title != 'null' && s.title != '' ORDER BY FIELD(s.id, " + ids + ") ASC");
            }
            else
            {
                selectStatement = conn.prepareStatement("SELECT sp.show_id, sp.filename, sp.type, s.id, s.title, s.imdb_id, s.year" + (getOverview ? ", s.overview" : "") + " FROM shows s WHERE id in (" + ids + ") AND s.title IS NOT NUll AND s.title != 'null' && s.title != '' GROUP BY s.id ORDER BY FIELD(s.id, " + ids + ") ASC");
            }

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                Show show = new Show();
                show.id = resultSet.getInt("id");
                show.title = resultSet.getString("title");
                show.imdb = resultSet.getString("imdb_id");
                show.year = resultSet.getInt("year");
                if (getOverview) show.overview = resultSet.getString("overview");
                int sid = !withPoster ? -1 : resultSet.getInt("show_id");
                if(sid > 0)
                {
                    ShowPhoto showPhoto = new ShowPhoto();
                    showPhoto.showId = sid;
                    showPhoto.type = resultSet.getString("type");
                    showPhoto.filename = resultSet.getString("filename");
                    show.showPhoto = showPhoto;
                }
                shows.add(show);
            }

            return shows;
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return shows;
        }
    }


    public List<ShowPhoto> getPopularShowPhotos(boolean withShow)
    {
        List<ShowPhoto> photos = new ArrayList<>(100);
        try
        {
            PreparedStatement selectStatement;
            if (!withShow)
            {
                selectStatement = conn.prepareStatement("SELECT sp.show_id, sp.filename, ps.ord FROM show_photo sp INNER JOIN popular_shows ps ON sp.show_id = ps.show_id WHERE sp.type = ? ORDER BY sp.show_id ASC");
            }
            else
            {
                selectStatement = conn.prepareStatement("SELECT s.id, s.title, s.year, sp.show_id, sp.filename, ps.ord FROM show_photo sp INNER JOIN popular_shows ps ON sp.show_id = ps.show_id LEFT JOIN shows s ON s.id = ps.show_id WHERE sp.type = ? ORDER BY sp.show_id ASC");
            }
            selectStatement.setString(1, "fanart");

            ResultSet resultSet = selectStatement.executeQuery();

            List<ShowPhoto> tempList = new ArrayList<>();
            int lastShowId = -1;

            while (resultSet.next())
            {
                ShowPhoto showPhoto = new ShowPhoto();
                showPhoto.showId = resultSet.getInt("show_id");
                showPhoto.filename = resultSet.getString("filename");
                showPhoto.ord = resultSet.getInt("ord");

                int sid = !withShow ? -1 : resultSet.getInt("id");
                if(sid > 0)
                {
                    Show show = new Show();
                    show.id = sid;
                    show.title = resultSet.getString("title");
                    show.year = resultSet.getInt("year");
                    showPhoto.show = show;
                }

                if(lastShowId != showPhoto.showId)
                {
                    if(!tempList.isEmpty())
                    {
                        //choose one from list and add it to photos
                        int rand = randInt(0, tempList.size() - 1);//randInt is inclusive (min max)
                        photos.add(tempList.get(rand));
                    }
                    tempList.clear();
                }
                lastShowId = showPhoto.showId;
                tempList.add(showPhoto);
            }

            Collections.sort(photos);

            return photos;
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return photos;
        }
    }

    public int getLastShowId()
    {
        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement("SELECT id FROM shows ORDER BY id DESC LIMIT 1;");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return -1;

            return resultSet.getInt("id");
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return -1;
        }
    }

    public Integer[] getInvalidIds()
    {
        try
        {
            List<Integer> ids = new ArrayList<Integer>(400000);

            PreparedStatement selectStatement = conn.prepareStatement("(SELECT id FROM shows) UNION (SELECT id FROM invalid_ids) ORDER BY id ASC");

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                ids.add(resultSet.getInt("id"));
            }

            return ids.toArray(new Integer[ids.size()]);
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return new Integer[0];
        }
    }

    public Integer[] getInvalidIdsForDownload(String type)
    {
        try
        {
            List<Integer> ids = new ArrayList<Integer>(400000);

            PreparedStatement selectStatement = conn.prepareStatement("(SELECT show_id AS id FROM show_photo WHERE type = ?) UNION (SELECT id FROM photo_invalid_ids) ORDER BY id ASC");
            selectStatement.setString(1, type);

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                ids.add(resultSet.getInt("id"));
            }

            return ids.toArray(new Integer[ids.size()]);
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return new Integer[0];
        }
    }

    public Integer[] getPhotoInvalidIds()
    {
        try
        {
            List<Integer> ids = new ArrayList<Integer>(400000);

            PreparedStatement selectStatement = conn.prepareStatement("(SELECT id FROM shows) UNION (SELECT id FROM photo_invalid_ids) ORDER BY id ASC");

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next())
            {
                ids.add(resultSet.getInt("id"));
            }

            return ids.toArray(new Integer[ids.size()]);
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return new Integer[0];
        }
    }

    public int deleteAll(String table)
    {
        try
        {
            PreparedStatement statement = conn.prepareStatement("DELETE FROM " + table);

            return statement.executeUpdate();
        }
        catch (SQLException e)
        {
            //fail silently
            if (debug)
                e.printStackTrace();
            LOGGER.warning(e.getMessage());
            return -1;
        }
    }

    public void close()
    {
        try
        {
            if (conn != null)
                conn.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean isInitialized()
    {
        return instance != null;
    }

    public int randInt(int min, int max)
    {
        return random.nextInt((max - min) + 1) + min;
    }

}