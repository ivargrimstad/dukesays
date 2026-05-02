package dukes.resource;

import dukes.model.MovieResponse;
import dukes.model.ProfileResponse;
import dukes.service.ProfileBuilder;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("profile")
@Produces(MediaType.APPLICATION_JSON)
public class ProfileResource {

    @Inject
    private ProfileBuilder profileBuilder;

    @GET
    @Path("{userId}")
    public Response getProfile(@PathParam("userId") Long userId) {
        ProfileResponse profile = profileBuilder.getProfile(userId);
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(profile).build();
    }

    @GET
    @Path("{userId}/movies")
    public Response getMovies(@PathParam("userId") Long userId) {
        List<MovieResponse> movies = profileBuilder.getMovies(userId);
        return Response.ok(movies).build();
    }
}