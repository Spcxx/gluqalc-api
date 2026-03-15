package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Basic user profile information")
public record UserResponse(
        @Schema(description = "Unique user identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "Registered email address", example = "user@example.com")
        String email,
        @Schema(description = "Account registration timestamp (UTC)", example = "2026-01-01T12:00:00Z")
        Instant createdAt
) {
}