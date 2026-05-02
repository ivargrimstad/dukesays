package dukes.service;

import dev.langchain4j.agent.tool.Tool;
import dukes.entity.UserProfile;
import dukes.model.MovieResponse;
import dukes.model.ProfileResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import java.util.List;

@ApplicationScoped
public class AgentTools {

    @Inject
    private ProfileBuilder profileBuilder;

    @Inject
    private TmdbService tmdbService;

    @Tool("Sync user profile from TMDB to the database. Call this at the start of each session to ensure data is fresh.")
    public String syncProfile(long userId) {
        try {
            JsonObject account = tmdbService.getAccount();
            String username = account.getString("username");

            var rated = tmdbService.getRatedMovies(userId);
            var favorites = tmdbService.getFavoriteMovies(userId);
            var watchlist = tmdbService.getWatchlistMovies(userId);

            profileBuilder.syncFromTmdb(userId, username, rated, favorites, watchlist);

            int total = rated.size() + favorites.size() + watchlist.size();
            return "Synced " + total + " movies for user " + username;
        } catch (Exception e) {
            return "Sync failed: " + e.getMessage();
        }
    }

    @Tool("Get the user's taste profile including favorite genres, top directors, top rated films, preferred decades, and average rating.")
    public String getUserProfile(long userId) {
        ProfileResponse profile = profileBuilder.getProfile(userId);
        if (profile == null) {
            return "No profile found for user " + userId + ". Try syncing first.";
        }
        return "Favorite genres: " + profile.favoriteGenres()
                + "\nTop directors: " + profile.topDirectors()
                + "\nTop rated films: " + profile.topRatedFilms()
                + "\nAverage rating: " + String.format("%.1f", profile.averageRating())
                + "\nPreferred decades: " + profile.preferredDecades()
                + "\nTheme preferences: " + profile.themePreferences();
    }

    @Tool("Get the list of movies the user has rated, favorited, or added to their watchlist.")
    public String getUserMovies(long userId) {
        List<MovieResponse> movies = profileBuilder.getMovies(userId);
        if (movies.isEmpty()) {
            return "No movies found for user " + userId;
        }
        StringBuilder sb = new StringBuilder();
        for (MovieResponse m : movies) {
            sb.append(m.title()).append(" (").append(m.year()).append(")");
            if (m.userRating() != null) {
                sb.append(" - Rating: ").append(m.userRating());
            }
            sb.append(" [").append(m.source()).append("]");
            sb.append(" Genres: ").append(m.genres());
            if (m.director() != null) {
                sb.append(" Dir: ").append(m.director());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Tool("Search for movies on TMDB by title. Returns up to 5 results with TMDB IDs.")
    public String searchMovies(String query) {
        List<JsonObject> results = tmdbService.searchMovies(query);
        if (results.isEmpty()) {
            return "No movies found for: " + query;
        }
        StringBuilder sb = new StringBuilder();
        for (JsonObject movie : results) {
            sb.append("ID: ").append(movie.getInt("id"))
                    .append(" - ").append(movie.getString("title", "Unknown"))
                    .append(" (").append(movie.getString("release_date", "N/A"), 0,
                            Math.min(4, movie.getString("release_date", "N/A").length()))
                    .append(")\n");
        }
        return sb.toString();
    }

    @Tool("Rate a movie. Only call this when the user explicitly asks to rate a movie. Requires the TMDB movie ID and a rating from 1 to 10.")
    public String rateMovie(long userId, long tmdbMovieId, double rating) {
        try {
            profileBuilder.rateMovie(userId, tmdbMovieId, rating);
            return "Rated movie " + tmdbMovieId + " with " + rating + "/10";
        } catch (Exception e) {
            return "Failed to rate: " + e.getMessage();
        }
    }

    @Tool("Add or remove a movie from the user's favorites. Only call when the user explicitly asks.")
    public String favoriteMovie(long userId, long tmdbMovieId, boolean favorite) {
        try {
            profileBuilder.favoriteMovie(userId, tmdbMovieId, favorite);
            return favorite ? "Added to favorites" : "Removed from favorites";
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    @Tool("Add or remove a movie from the user's watchlist. Only call when the user explicitly asks.")
    public String watchlistMovie(long userId, long tmdbMovieId, boolean add) {
        try {
            profileBuilder.watchlistMovie(userId, tmdbMovieId, add);
            return add ? "Added to watchlist" : "Removed from watchlist";
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    @Tool("Get detailed information about a specific movie from TMDB, including cast, crew, and genres.")
    public String getMovieDetails(long tmdbMovieId) {
        JsonObject details = tmdbService.getMovieDetails(tmdbMovieId);
        String title = details.getString("title", "Unknown");
        String overview = details.getString("overview", "No overview");
        String releaseDate = details.getString("release_date", "N/A");
        int runtime = details.getInt("runtime", 0);

        StringBuilder sb = new StringBuilder();
        sb.append(title).append(" (").append(releaseDate, 0, Math.min(4, releaseDate.length())).append(")\n");
        sb.append("Runtime: ").append(runtime).append(" min\n");
        sb.append("Overview: ").append(overview).append("\n");

        if (details.containsKey("genres")) {
            var genres = details.getJsonArray("genres");
            sb.append("Genres: ");
            for (int i = 0; i < genres.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(genres.getJsonObject(i).getString("name"));
            }
            sb.append("\n");
        }

        if (details.containsKey("credits")) {
            var crew = details.getJsonObject("credits").getJsonArray("crew");
            for (int i = 0; i < crew.size(); i++) {
                var member = crew.getJsonObject(i);
                if ("Director".equals(member.getString("job", ""))) {
                    sb.append("Director: ").append(member.getString("name")).append("\n");
                    break;
                }
            }
        }

        return sb.toString();
    }
}