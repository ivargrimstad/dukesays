package dukes.resource;

import dukes.model.FavoriteRequest;
import dukes.model.RateRequest;
import dukes.model.WatchlistRequest;
import dukes.service.ProfileBuilder;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {

    @Inject
    private ProfileBuilder profileBuilder;

    @POST
    @Path("rate")
    public Response rateMovie(RateRequest request) {
        try {
            profileBuilder.rateMovie(request.userId(), request.tmdbMovieId(), request.rating());
            return Response.ok("{\"status\":\"rated\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("favorite")
    public Response favoriteMovie(FavoriteRequest request) {
        try {
            profileBuilder.favoriteMovie(request.userId(), request.tmdbMovieId(), request.favorite());
            return Response.ok("{\"status\":\"updated\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("watchlist")
    public Response watchlistMovie(WatchlistRequest request) {
        try {
            profileBuilder.watchlistMovie(request.userId(), request.tmdbMovieId(), request.watchlist());
            return Response.ok("{\"status\":\"updated\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}