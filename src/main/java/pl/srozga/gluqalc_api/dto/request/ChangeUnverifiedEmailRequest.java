package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for correcting an email address before the account is fully verified")
public record ChangeUnverifiedEmailRequest(
        @Schema(description = "The incorrect email address originally used for registration", example = "typo.email@example.com")
        @NotBlank(message = "Old email is required")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
        @Email(message = "Invalid old email format")
        String oldEmail,
        @Schema(description = "User's password for security verification", example = "MySecretPass123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String password,
        @Schema(description = "The correct new email address", example = "correct.email@example.com")
        @NotBlank(message = "New email is required")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
        @Email(message = "Invalid new email format")
        String newEmail
) {
}