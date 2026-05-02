package dukes.resource;

import dukes.model.RecommendRequest;
import dukes.model.Recommendation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("recommend")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecommendationResource {

    @POST
    public Response recommend(RecommendRequest request) {
        // TODO: fetch profile, call RecommendationService
        return Response.ok(List.of()).build();
    }
}