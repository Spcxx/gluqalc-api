package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record AcceptConsentsRequest(
        @NotEmpty(message = "You must provide at least one consent ID to accept")
        List<UUID> consentDefinitionIds,
        @NotBlank(message = "Refresh token cannot be blank")
        String refreshToken
) {
}
