package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "ID Token cannot be blank")
        String idToken
) {
}
