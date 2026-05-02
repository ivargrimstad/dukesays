package dukes.resource;

import dukes.service.TmdbService;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("tmdb")
@Produces(MediaType.APPLICATION_JSON)
public class TmdbResource {

    @Inject
    private TmdbService tmdbService;

    @GET
    @Path("auth")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startAuth() throws Exception {
        String token = tmdbService.createRequestToken();
        String approvalUrl = tmdbService.getApprovalUrl(token);
        String json = "{\"request_token\":\"" + token + "\",\"approval_url\":\"" + approvalUrl + "\"}";
        return Response.ok(json).build();
    }

    @GET
    @Path("session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSession(@QueryParam("request_token") String requestToken) throws Exception {
        String sessionId = tmdbService.createSession(requestToken);
        JsonObject account = tmdbService.getAccount();
        String json = "{\"session_id\":\"" + sessionId + "\",\"username\":\"" + account.getString("username") + "\",\"account_id\":" + account.getInt("id") + "}";
        return Response.ok(json).build();
    }

    @GET
    @Path("rated")
    public Response getRated(@QueryParam("account_id") long accountId) throws Exception {
        List<JsonObject> movies = tmdbService.getRatedMovies(accountId);
        return Response.ok(movies.toString()).build();
    }

    @GET
    @Path("favorites")
    public Response getFavorites(@QueryParam("account_id") long accountId) throws Exception {
        List<JsonObject> movies = tmdbService.getFavoriteMovies(accountId);
        return Response.ok(movies.toString()).build();
    }

    @GET
    @Path("watchlist")
    public Response getWatchlist(@QueryParam("account_id") long accountId) throws Exception {
        List<JsonObject> movies = tmdbService.getWatchlistMovies(accountId);
        return Response.ok(movies.toString()).build();
    }
}
