package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.srozga.gluqalc_api.common.AuthProvider;
import pl.srozga.gluqalc_api.common.UserRole;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Comprehensive user account data for administrative management")
public record UserAdminResponse(
        @Schema(description = "Unique user identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "User's email address", example = "user@example.com")
        String email,
        @Schema(description = "Set of roles assigned to the user", example = "[\"USER\", \"ADMIN\"]")
        Set<UserRole> roles,
        @Schema(description = "Authentication providers linked to this account", example = "[\"LOCAL\", \"GOOGLE\"]")
        Set<AuthProvider> provider,
        @Schema(description = "Indicates if the email has been verified", example = "true")
        boolean enabled,
        @Schema(description = "Indicates if the account is locked (e.g., due to too many failed logins)", example = "false")
        boolean locked,
        @Schema(description = "Indicates if the account has been soft-deleted", example = "false")
        boolean deleted,
        @Schema(description = "Account creation timestamp (UTC)", example = "2026-01-01T12:00:00Z")
        Instant createdAt,
        @Schema(description = "Last account update timestamp (UTC)", example = "2026-03-15T10:15:30Z")
        Instant updatedAt
) {
}