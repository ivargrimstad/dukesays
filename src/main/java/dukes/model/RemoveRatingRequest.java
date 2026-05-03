package dukes.model;

public record RemoveRatingRequest(
        Long userId,
        Long tmdbMovieId
) {}