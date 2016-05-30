package rs.pedjaapps.moviewallpapers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import rs.pedjaapps.moviewallpapers.model.ShowPhoto;

@Path("/photos")
public class Photos
{
    @GET
    @Path("/list")
    public Response getPhotos(@QueryParam("page") int page, @QueryParam("per_page") int perPage, @QueryParam("with_show") boolean withShow)
    {
        if(perPage <= 0)
            perPage = Constants.DEFAULT_PER_PAGE;
        if(page < 1)
            page = 1;

        List<ShowPhoto> shows = DatabaseManager.getInstance().getShowPhotos(page, perPage, withShow);
        boolean hasNext = shows.size() > perPage;

        try
        {
            JSONObject data = new JSONObject();
            data.put("has_next", hasNext);
            data.put("page", page);
            data.put("per_page", perPage);
            JSONArray jShowPhotos = new JSONArray();
            for(ShowPhoto showPhoto : shows)
            {
                JSONObject jShowPhoto = new JSONObject();
                jShowPhoto.put("show_id", showPhoto.showId);
                jShowPhoto.put("filename", showPhoto.filename);
                if(showPhoto.show != null)
                {
                    JSONObject jShow = new JSONObject();
                    jShow.put("title", showPhoto.show.title);
                    jShow.put("year", showPhoto.show.year);
                    jShowPhoto.put("show", jShow);
                }
                jShowPhotos.put(jShowPhoto);
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
    @Path("/popular")
    public Response getPopularPhotos(@QueryParam("with_show") boolean withShow)
    {
        List<ShowPhoto> shows = DatabaseManager.getInstance().getPopularShowPhotos(withShow);

        try
        {
            JSONObject data = new JSONObject();
            JSONArray jShowPhotos = new JSONArray();
            for(ShowPhoto showPhoto : shows)
            {
                JSONObject jShowPhoto = new JSONObject();
                jShowPhoto.put("show_id", showPhoto.showId);
                jShowPhoto.put("filename", showPhoto.filename);
                if(showPhoto.show != null)
                {
                    JSONObject jShow = new JSONObject();
                    jShow.put("title", showPhoto.show.title);
                    jShow.put("year", showPhoto.show.year);
                    jShowPhoto.put("show", jShow);
                }
                jShowPhotos.put(jShowPhoto);
            }
            data.put("show_photos", jShowPhotos);
            return Response.ok(data.toString(), MediaType.APPLICATION_JSON).build();
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }
}