package dukes.model;

import java.util.List;

public record ProfileResponse(
        Long userId,
        List<String> favoriteGenres,
        List<String> topDirectors,
        List<String> topRatedFilms,
        double averageRating,
        List<String> preferredDecades,
        List<String> themePreferences
) {}