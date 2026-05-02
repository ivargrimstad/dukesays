package dukes.model;

public record SyncResponse(
        Long userId,
        String status,
        String message
) {}