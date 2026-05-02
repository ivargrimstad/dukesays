package dukes.model;

public record WatchlistRequest(
        Long userId,
        Long tmdbMovieId,
        boolean watchlist
) {}