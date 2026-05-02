package dukes.resource;

import dukes.service.MovieAgent;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

@Path("chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    private MovieAgent movieAgent;

    @Resource
    private ManagedExecutorService executor;

    public record ChatRequest(Long userId, String message) {}
    public record ChatResponse(String reply) {}

    @POST
    public void chat(@Suspended AsyncResponse asyncResponse, ChatRequest request) {
        asyncResponse.setTimeout(120, TimeUnit.SECONDS);

        executor.submit(() -> {
            try {
                String messageWithContext = "[User ID: " + request.userId() + "] " + request.message();
                String reply = movieAgent.chat(request.userId(), messageWithContext);
                asyncResponse.resume(Response.ok(new ChatResponse(reply)).build());
            } catch (Exception e) {
                asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ChatResponse("Error: " + e.getMessage())).build());
            }
        });
    }
}