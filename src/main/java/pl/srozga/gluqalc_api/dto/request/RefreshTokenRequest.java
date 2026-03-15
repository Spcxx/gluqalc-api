package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token cannot be blank")
        @Size(max = 5000, message = "Refresh token is too long")
        String refreshToken
) {
}
