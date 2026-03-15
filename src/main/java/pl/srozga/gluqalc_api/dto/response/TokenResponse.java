package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication tokens and session information issued upon successful login or token refresh")
public record TokenResponse(
        @Schema(description = "Access token (JWT) used for authenticating subsequent API requests", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String jwtToken,
        @Schema(description = "Long-lived refresh token used to obtain new access tokens", example = "d8e9f0a1-b2c3-4d5e-6f7g-8h9i0j1k2l3m")
        String refreshToken,
        @Schema(description = "Number of seconds until the access token expires", example = "3600")
        long expiresIn,
        @Schema(description = "The device ID associated with this session", example = "android-pixel-7-xyz-987")
        String deviceId
) {
}