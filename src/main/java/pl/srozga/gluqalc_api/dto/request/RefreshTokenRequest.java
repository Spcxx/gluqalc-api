package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token cannot be blank")
        String refreshToken
) {
}
