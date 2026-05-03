package dukes.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TmdbService {

    private static final String BASE_URL = "https://api.themoviedb.org/3";

    private Client client;

    private static final Map<Integer, String> GENRE_MAP = Map.ofEntries(
            Map.entry(28, "Action"), Map.entry(12, "Adventure"), Map.entry(16, "Animation"),
            Map.entry(35, "Comedy"), Map.entry(80, "Crime"), Map.entry(99, "Documentary"),
            Map.entry(18, "Drama"), Map.entry(10751, "Family"), Map.entry(14, "Fantasy"),
            Map.entry(36, "History"), Map.entry(27, "Horror"), Map.entry(10402, "Music"),
            Map.entry(9648, "Mystery"), Map.entry(10749, "Romance"), Map.entry(878, "Sci-Fi"),
            Map.entry(10770, "TV Movie"), Map.entry(53, "Thriller"), Map.entry(10752, "War"),
            Map.entry(37, "Western")
    );

    public static String genreIdToName(int id) {
        return GENRE_MAP.getOrDefault(id, "Unknown");
    }

    private String apiKey;
    private String sessionId;

    @PostConstruct
    public void init() {
        client = ClientBuilder.newClient();
        apiKey = System.getenv("TMDB_API_KEY");
    }

    @PreDestroy
    public void cleanup() {
        if (client != null) {
            client.close();
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String createRequestToken() {
        JsonObject response = get("/authentication/token/new");
        return response.getString("request_token");
    }

    public String getApprovalUrl(String requestToken) {
        return "https://www.themoviedb.org/authenticate/" + requestToken;
    }

    public String createSession(String requestToken) {
        String body = Json.createObjectBuilder()
                .add("request_token", requestToken)
                .build()
                .toString();

        JsonObject response = post("/authentication/session/new", body);

        if (!response.getBoolean("success", false)) {
            String msg = response.getString("status_message", "Token not approved. Please approve on TMDB first.");
            throw new RuntimeException(msg);
        }

        this.sessionId = response.getString("session_id");
        return sessionId;
    }

    public JsonObject getAccount() {
        return get("/account?session_id=" + sessionId);
    }

    public List<JsonObject> getRatedMovies(long accountId) {
        return fetchAllPages("/account/" + accountId + "/rated/movies?session_id=" + sessionId);
    }

    public List<JsonObject> getFavoriteMovies(long accountId) {
        return fetchAllPages("/account/" + accountId + "/favorite/movies?session_id=" + sessionId);
    }

    public List<JsonObject> getWatchlistMovies(long accountId) {
        return fetchAllPages("/account/" + accountId + "/watchlist/movies?session_id=" + sessionId);
    }

    public JsonObject getMovieDetails(long movieId) {
        return get("/movie/" + movieId + "?append_to_response=credits");
    }

    public void rateMovie(long movieId, double rating) {
        String body = Json.createObjectBuilder()
                .add("value", rating)
                .build()
                .toString();
        post("/movie/" + movieId + "/rating?session_id=" + sessionId, body);
    }

    public void deleteRating(long movieId) {
        delete("/movie/" + movieId + "/rating?session_id=" + sessionId);
    }

    public void setFavorite(long accountId, long movieId, boolean favorite) {
        String body = Json.createObjectBuilder()
                .add("media_type", "movie")
                .add("media_id", movieId)
                .add("favorite", favorite)
                .build()
                .toString();
        post("/account/" + accountId + "/favorite?session_id=" + sessionId, body);
    }

    public void setWatchlist(long accountId, long movieId, boolean watchlist) {
        String body = Json.createObjectBuilder()
                .add("media_type", "movie")
                .add("media_id", movieId)
                .add("watchlist", watchlist)
                .build()
                .toString();
        post("/account/" + accountId + "/watchlist?session_id=" + sessionId, body);
    }

    public List<JsonObject> searchMovies(String query) {
        JsonObject response = get("/search/movie?query=" + query.replace(" ", "+"));
        JsonArray results = response.getJsonArray("results");
        List<JsonObject> movies = new ArrayList<>();
        for (int i = 0; i < Math.min(results.size(), 5); i++) {
            movies.add(results.getJsonObject(i));
        }
        return movies;
    }

    private List<JsonObject> fetchAllPages(String path) {
        List<JsonObject> allResults = new ArrayList<>();
        int page = 1;
        int totalPages;

        do {
            String separator = path.contains("?") ? "&" : "?";
            JsonObject response = get(path + separator + "page=" + page);
            JsonArray results = response.getJsonArray("results");
            for (int i = 0; i < results.size(); i++) {
                allResults.add(results.getJsonObject(i));
            }
            totalPages = response.getInt("total_pages", 1);
            page++;
        } while (page <= totalPages);

        return allResults;
    }

    private JsonObject get(String path) {
        String separator = path.contains("?") ? "&" : "?";
        String url = BASE_URL + path + separator + "api_key=" + apiKey;

        Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .get();

        String body = response.readEntity(String.class);
        response.close();

        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            return reader.readObject();
        }
    }

    private JsonObject post(String path, String jsonBody) {
        String separator = path.contains("?") ? "&" : "?";
        String url = BASE_URL + path + separator + "api_key=" + apiKey;

        Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(jsonBody));

        String body = response.readEntity(String.class);
        response.close();

        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            return reader.readObject();
        }
    }

    private JsonObject delete(String path) {
        String separator = path.contains("?") ? "&" : "?";
        String url = BASE_URL + path + separator + "api_key=" + apiKey;

        Response response = client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        String body = response.readEntity(String.class);
        response.close();

        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            return reader.readObject();
        }
    }
}
