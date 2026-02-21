package pl.srozga.gluqalc_api.dto.response;

import java.time.Instant;

public record DeviceSessionResponse(
        String deviceId,
        String ipAddress,
        String userAgent,
        Instant lastAccessedAt,
        Instant expiresAt
) {
}
