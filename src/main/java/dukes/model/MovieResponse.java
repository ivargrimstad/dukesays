package dukes.model;

public record MovieResponse(
        Long tmdbMovieId,
        String title,
        int year,
        String genres,
        String director,
        String posterPath,
        Double userRating,
        String source
) {}