package pl.srozga.gluqalc_api.dto.response;

public record TokenResponse(
        String jwtToken,
        String refreshToken,
        long expiresIn,
        String deviceId
) {
}
