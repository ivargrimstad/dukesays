package dukes.model;

import java.util.List;

public record Recommendation(
        String title,
        int year,
        String reasoning,
        double matchScore,
        List<String> matchedPreferences
) {}