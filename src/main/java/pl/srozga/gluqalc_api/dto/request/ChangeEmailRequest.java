package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for initiating an email change for a verified account")
public record ChangeEmailRequest(
        @Schema(description = "The new email address the user wants to use", example = "new.email@example.com")
        @NotBlank(message = "New email is required")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
        @Email(message = "Invalid email format")
        String newEmail,
        @Schema(description = "User's current password for security verification", example = "MySecretPass123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String password
) {
}