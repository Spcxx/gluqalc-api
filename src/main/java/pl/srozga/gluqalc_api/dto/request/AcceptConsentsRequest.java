package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request payload for accepting pending legal consents and policies")
public record AcceptConsentsRequest(
        @Schema(description = "List of UUIDs representing the specific consent definitions the user is accepting", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
        @NotEmpty(message = "You must provide at least one consent ID to accept")
        @Size(max = 20, message = "You cannot process more than 20 consents at once")
        List<@NotNull(message = "Consent ID cannot be null") UUID> consentDefinitionIds,
        @Schema(description = "Current valid refresh token, used to rotate the session and update claims after consent acceptance", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank(message = "Refresh token cannot be blank")
        @Size(max = 512, message = "Refresh token is too long")
        String refreshToken
) {
}