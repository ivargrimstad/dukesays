package dukes.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TmdbService {

    private static final String BASE_URL = "https://api.themoviedb.org/3";

    private final HttpClient httpClient = HttpClient.newHttpClient();

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

    private String apiKey = "5fb797fb41371c74d9d12c26935f40a3";
    private String sessionId;

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

    public String createRequestToken() throws Exception {
        JsonObject response = get("/authentication/token/new");
        return response.getString("request_token");
    }

    public String getApprovalUrl(String requestToken) {
        return "https://www.themoviedb.org/authenticate/" + requestToken;
    }

    public String createSession(String requestToken) throws Exception {
        String body = Json.createObjectBuilder()
                .add("request_token", requestToken)
                .build()
                .toString();

        JsonObject response = post("/authentication/session/new", body);
        this.sessionId = response.getString("session_id");
        return sessionId;
    }

    public JsonObject getAccount() throws Exception {
        return get("/account?session_id=" + sessionId);
    }

    public List<JsonObject> getRatedMovies(long accountId) throws Exception {
        return fetchAllPages("/account/" + accountId + "/rated/movies?session_id=" + sessionId);
    }

    public List<JsonObject> getFavoriteMovies(long accountId) throws Exception {
        return fetchAllPages("/account/" + accountId + "/favorite/movies?session_id=" + sessionId);
    }

    public List<JsonObject> getWatchlistMovies(long accountId) throws Exception {
        return fetchAllPages("/account/" + accountId + "/watchlist/movies?session_id=" + sessionId);
    }

    public JsonObject getMovieDetails(long movieId) throws Exception {
        return get("/movie/" + movieId + "?append_to_response=credits");
    }

    private List<JsonObject> fetchAllPages(String path) throws Exception {
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

    private JsonObject get(String path) throws Exception {
        String separator = path.contains("?") ? "&" : "?";
        String url = BASE_URL + path + separator + "api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            return reader.readObject();
        }
    }

    private JsonObject post(String path, String body) throws Exception {
        String separator = path.contains("?") ? "&" : "?";
        String url = BASE_URL + path + separator + "api_key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
            return reader.readObject();
        }
    }
}
