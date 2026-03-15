package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record AcceptConsentsRequest(
        @NotEmpty(message = "You must provide at least one consent ID to accept")
        @Size(max = 20, message = "You cannot process more than 20 consents at once")
        List<@NotNull(message = "Consent ID cannot be null") UUID> consentDefinitionIds,
        @NotBlank(message = "Refresh token cannot be blank")
        @Size(max = 512, message = "Refresh token is too long")
        String refreshToken
) {
}
