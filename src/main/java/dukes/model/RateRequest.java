package dukes.model;

public record RateRequest(
        Long userId,
        Long tmdbMovieId,
        double rating
) {}