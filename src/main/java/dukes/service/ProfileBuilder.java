package dukes.service;

import dukes.entity.Movie;
import dukes.entity.UserMovie;
import dukes.entity.UserProfile;
import dukes.model.MovieResponse;
import dukes.model.ProfileResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ProfileBuilder {

    @PersistenceContext(unitName = "knowme")
    private EntityManager em;

    @Inject
    private TmdbService tmdbService;

    public ProfileResponse getProfile(Long userId) {
        UserProfile profile = findByTmdbUserId(userId);
        if (profile == null) {
            return null;
        }
        return toResponse(profile);
    }

    public List<MovieResponse> getMovies(Long userId) {
        var results = em.createQuery(
                "SELECT um FROM UserMovie um JOIN FETCH um.movie WHERE um.userProfile.tmdbUserId = :userId", UserMovie.class)
                .setParameter("userId", userId)
                .getResultList();
        return results.stream()
                .map(this::toMovieResponse)
                .toList();
    }

    @Transactional
    public UserProfile syncFromTmdb(Long tmdbUserId, String username,
                                    List<JsonObject> ratedMovies,
                                    List<JsonObject> favoriteMovies,
                                    List<JsonObject> watchlistMovies) {

        UserProfile profile = findByTmdbUserId(tmdbUserId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setTmdbUserId(tmdbUserId);
        }
        profile.setUsername(username);

        profile.getMovies().clear();
        em.flush();

        addMovies(profile, ratedMovies, UserMovie.Source.RATED);
        addMovies(profile, favoriteMovies, UserMovie.Source.FAVORITE);
        addMovies(profile, watchlistMovies, UserMovie.Source.WATCHLIST);

        analyzeProfile(profile);
        profile.setLastSyncedAt(LocalDateTime.now());

        if (profile.getId() == null) {
            em.persist(profile);
        } else {
            em.merge(profile);
        }

        return profile;
    }

    private void addMovies(UserProfile profile, List<JsonObject> tmdbMovies, UserMovie.Source source) {
        for (JsonObject tmdbMovie : tmdbMovies) {
            long tmdbMovieId = tmdbMovie.getJsonNumber("id").longValue();

            Movie movie = findOrCreateMovie(tmdbMovieId, tmdbMovie);

            UserMovie userMovie = new UserMovie();
            userMovie.setUserProfile(profile);
            userMovie.setMovie(movie);
            userMovie.setSource(source);

            if (source == UserMovie.Source.RATED && tmdbMovie.containsKey("rating")) {
                userMovie.setUserRating(tmdbMovie.getJsonNumber("rating").doubleValue());
            }

            profile.getMovies().add(userMovie);
        }
    }

    private Movie findOrCreateMovie(long tmdbMovieId, JsonObject tmdbMovie) {
        var results = em.createQuery(
                "SELECT m FROM Movie m WHERE m.tmdbMovieId = :tmdbId", Movie.class)
                .setParameter("tmdbId", tmdbMovieId)
                .getResultList();

        if (!results.isEmpty()) {
            return results.getFirst();
        }

        Movie movie = new Movie();
        movie.setTmdbMovieId(tmdbMovieId);
        movie.setTitle(tmdbMovie.getString("title", "Unknown"));

        String releaseDate = tmdbMovie.getString("release_date", "");
        if (!releaseDate.isBlank() && releaseDate.length() >= 4) {
            movie.setYear(Integer.parseInt(releaseDate.substring(0, 4)));
        }

        if (tmdbMovie.containsKey("genre_ids")) {
            JsonArray genreIds = tmdbMovie.getJsonArray("genre_ids");
            List<String> genres = new ArrayList<>();
            for (int i = 0; i < genreIds.size(); i++) {
                genres.add(TmdbService.genreIdToName(genreIds.getInt(i)));
            }
            movie.setGenres(String.join(",", genres));
        }

        movie.setPosterPath(tmdbMovie.getString("poster_path", null));

        try {
            JsonObject details = tmdbService.getMovieDetails(tmdbMovieId);
            if (details.containsKey("credits")) {
                JsonArray crew = details.getJsonObject("credits").getJsonArray("crew");
                for (int i = 0; i < crew.size(); i++) {
                    JsonObject member = crew.getJsonObject(i);
                    if ("Director".equals(member.getString("job", ""))) {
                        movie.setDirector(member.getString("name"));
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        em.persist(movie);
        return movie;
    }

    private void analyzeProfile(UserProfile profile) {
        Map<String, Integer> genreCounts = new HashMap<>();
        Map<String, Integer> directorCounts = new HashMap<>();
        List<String> topRated = new ArrayList<>();
        Map<String, Integer> decadeCounts = new HashMap<>();
        double totalRating = 0;
        int ratedCount = 0;

        for (UserMovie um : profile.getMovies()) {
            Movie movie = um.getMovie();

            if (movie.getGenres() != null) {
                for (String genre : movie.getGenres().split(",")) {
                    genreCounts.merge(genre.trim(), 1, Integer::sum);
                }
            }

            if (movie.getDirector() != null && !movie.getDirector().isBlank()) {
                directorCounts.merge(movie.getDirector(), 1, Integer::sum);
            }

            if (movie.getYear() > 0) {
                String decade = (movie.getYear() / 10 * 10) + "s";
                decadeCounts.merge(decade, 1, Integer::sum);
            }

            if (um.getUserRating() != null) {
                totalRating += um.getUserRating();
                ratedCount++;
                if (um.getUserRating() >= 8.0) {
                    topRated.add(movie.getTitle());
                }
            }
        }

        List<String> topGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        List<String> topDirectors = directorCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        List<String> topDecades = decadeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        profile.setFavoriteGenres(String.join(",", topGenres));
        profile.setTopDirectors(String.join(",", topDirectors));
        profile.setTopRatedFilms(String.join(",", topRated));
        profile.setPreferredDecades(String.join(",", topDecades));
        profile.setAverageRating(ratedCount > 0 ? totalRating / ratedCount : 0);
    }

    public UserProfile findByTmdbUserId(Long tmdbUserId) {
        var results = em.createQuery(
                "SELECT p FROM UserProfile p WHERE p.tmdbUserId = :tmdbUserId", UserProfile.class)
                .setParameter("tmdbUserId", tmdbUserId)
                .getResultList();
        return results.isEmpty() ? null : results.getFirst();
    }

    private ProfileResponse toResponse(UserProfile profile) {
        return new ProfileResponse(
                profile.getTmdbUserId(),
                splitToList(profile.getFavoriteGenres()),
                splitToList(profile.getTopDirectors()),
                splitToList(profile.getTopRatedFilms()),
                profile.getAverageRating(),
                splitToList(profile.getPreferredDecades()),
                splitToList(profile.getThemePreferences())
        );
    }

    private MovieResponse toMovieResponse(UserMovie um) {
        return new MovieResponse(
                um.getMovie().getTmdbMovieId(),
                um.getMovie().getTitle(),
                um.getMovie().getYear(),
                um.getMovie().getGenres(),
                um.getMovie().getDirector(),
                um.getMovie().getPosterPath(),
                um.getUserRating(),
                um.getSource().name()
        );
    }

    private List<String> splitToList(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.asList(csv.split(","));
    }
}