package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Information about an active device session associated with the user account")
public record DeviceSessionResponse(
        @Schema(description = "Unique identifier for the device", example = "android-xyz-123")
        String deviceId,
        @Schema(description = "Last known IP address of the session", example = "192.168.1.15")
        String ipAddress,
        @Schema(description = "User Agent string identifying the browser or app client", example = "Mozilla/5.0 (Linux; Android 12; Pixel 7)")
        String userAgent,
        @Schema(description = "Timestamp of the last activity in this session (UTC)", example = "2026-03-15T10:15:30Z")
        Instant lastAccessedAt,
        @Schema(description = "Timestamp when the session's refresh token will expire (UTC)", example = "2026-03-22T10:15:30Z")
        Instant expiresAt
) {
}