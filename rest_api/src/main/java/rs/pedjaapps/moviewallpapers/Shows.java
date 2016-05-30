package rs.pedjaapps.moviewallpapers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import rs.pedjaapps.moviewallpapers.model.Show;
import rs.pedjaapps.moviewallpapers.model.ShowPhoto;

@Path("/shows")
public class Shows
{
    @GET
    @Path("/{show_id}")
    public Response getShow(@PathParam("show_id") int showId, @QueryParam("with_poster") boolean withPoster)
    {
        Show show = DatabaseManager.getInstance().getShow(showId, withPoster);

        try
        {
            if (show != null)
            {
                JSONObject jShow = new JSONObject();
                jShow.put("id", show.id);
                jShow.put("title", show.title);
                jShow.put("imdb_id", show.imdb);
                jShow.put("year", show.year);
                jShow.put("overview", show.overview);
                if(show.showPhoto != null)
                {
                    JSONObject jShowPhoto = new JSONObject();
                    jShowPhoto.put("filename", show.showPhoto.filename);
                    jShow.put("poster", jShowPhoto);
                }
                return Response.ok(jShow.toString(), MediaType.APPLICATION_JSON).build();
            }
            else
            {
                JSONObject jError = new JSONObject();
                jError.put("code", Error.NOT_FOUND.toString());
                jError.put("message", "Show not found");
                return Response.ok(jError.toString(), MediaType.APPLICATION_JSON).build();
            }
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/{show_id}/photos")
    public Response getShowPhotos(@PathParam("show_id") int showId, @QueryParam("page") int page, @QueryParam("per_page") int perPage)
    {
        if(perPage <= 0)
            perPage = Constants.DEFAULT_PER_PAGE;
        if(page < 1)
            page = 1;

        List<ShowPhoto> shows = DatabaseManager.getInstance().getShowPhotos(showId, page, perPage);
        boolean hasNext = shows.size() > perPage;

        try
        {
            JSONObject data = new JSONObject();
            data.put("has_next", hasNext);
            data.put("page", page);
            data.put("per_page", perPage);
            JSONArray jShowPhotos = new JSONArray();
            for(ShowPhoto show : shows)
            {
                JSONObject jShow = new JSONObject();
                jShow.put("show_id", show.showId);
                jShow.put("filename", show.filename);
                jShowPhotos.put(jShow);
            }
            data.put("show_photos", jShowPhotos);
            return Response.ok(data.toString(), MediaType.APPLICATION_JSON).build();
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/list")
    public Response getShows(@QueryParam("page") int page, @QueryParam("per_page") int perPage, @QueryParam("get_overview") boolean getOverview, @QueryParam("with_poster") boolean withPoster)
    {
        if (perPage <= 0)
            perPage = Constants.DEFAULT_PER_PAGE;
        if (page < 1)
            page = 1;

        List<Show> shows = DatabaseManager.getInstance().getShows(page, perPage, getOverview, withPoster);
        boolean hasNext = shows.size() > perPage;

        try
        {
            JSONObject data = new JSONObject();
            data.put("has_next", hasNext);
            data.put("page", page);
            data.put("per_page", perPage);
            JSONArray jShows = new JSONArray();
            for (Show show : shows)
            {
                JSONObject jShow = new JSONObject();
                jShow.put("id", show.id);
                jShow.put("title", show.title);
                jShow.put("imdb_id", show.imdb);
                jShow.put("year", show.year);
                if (getOverview) jShow.put("overview", show.overview);
                if(show.showPhoto != null)
                {
                    JSONObject jShowPhoto = new JSONObject();
                    jShowPhoto.put("filename", show.showPhoto.filename);
                    jShow.put("poster", jShowPhoto);
                }
                jShows.put(jShow);
            }
            data.put("shows", jShows);
            return Response.ok(data.toString(), MediaType.APPLICATION_JSON).build();
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/popular")
    public Response getPopularShows(@QueryParam("get_overview") boolean getOverview, @QueryParam("with_poster") boolean withPoster)
    {
        List<Show> shows = DatabaseManager.getInstance().getPopularShows(getOverview, withPoster);

        try
        {
            JSONObject data = new JSONObject();
            JSONArray jShows = new JSONArray();
            for (Show show : shows)
            {
                JSONObject jShow = new JSONObject();
                jShow.put("id", show.id);
                jShow.put("title", show.title);
                jShow.put("imdb_id", show.imdb);
                jShow.put("year", show.year);
                if (getOverview) jShow.put("overview", show.overview);
                if(show.showPhoto != null)
                {
                    JSONObject jShowPhoto = new JSONObject();
                    jShowPhoto.put("filename", show.showPhoto.filename);
                    jShow.put("poster", jShowPhoto);
                }
                jShows.put(jShow);
            }
            data.put("shows", jShows);
            return Response.ok(data.toString(), MediaType.APPLICATION_JSON).build();
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }
}