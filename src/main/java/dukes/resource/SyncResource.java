package dukes.resource;

import dukes.model.SyncResponse;
import dukes.service.ProfileBuilder;
import dukes.service.TmdbService;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("sync")
@Produces(MediaType.APPLICATION_JSON)
public class SyncResource {

    @Inject
    private TmdbService tmdbService;

    @Inject
    private ProfileBuilder profileBuilder;

    @POST
    @Path("{accountId}")
    public Response sync(@PathParam("accountId") Long accountId) {
        try {
            JsonObject account = tmdbService.getAccount();
            String username = account.getString("username");

            var rated = tmdbService.getRatedMovies(accountId);
            var favorites = tmdbService.getFavoriteMovies(accountId);
            var watchlist = tmdbService.getWatchlistMovies(accountId);

            profileBuilder.syncFromTmdb(accountId, username, rated, favorites, watchlist);

            int total = rated.size() + favorites.size() + watchlist.size();
            return Response.ok(new SyncResponse(accountId, "success",
                    "Synced " + total + " movies (" + rated.size() + " rated, "
                            + favorites.size() + " favorites, " + watchlist.size() + " watchlist)")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new SyncResponse(accountId, "error", e.getMessage())).build();
        }
    }
}