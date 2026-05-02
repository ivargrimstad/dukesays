package dukes.model;

public record RecommendRequest(
        Long userId,
        String query
) {}