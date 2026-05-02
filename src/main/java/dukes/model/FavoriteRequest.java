package dukes.model;

public record FavoriteRequest(
        Long userId,
        Long tmdbMovieId,
        boolean favorite
) {}