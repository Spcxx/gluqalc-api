package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for refreshing an access token using an active Refresh Token")
public record RefreshTokenRequest(
        @Schema(description = "The valid refresh token issued during login", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank(message = "Refresh token cannot be blank")
        @Size(max = 5000, message = "Refresh token is too long")
        String refreshToken
) {
}