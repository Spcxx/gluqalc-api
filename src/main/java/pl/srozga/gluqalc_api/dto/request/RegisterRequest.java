package pl.srozga.gluqalc_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.srozga.gluqalc_api.validation.ValidPassword;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request payload for creating a new local user account")
public record RegisterRequest(
        @Schema(description = "The email address to register", example = "newuser@example.com")
        @NotBlank(message = "Email is required")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
        @Email(message = "Invalid email format")
        String email,
        @Schema(description = "The password for the new account", example = "MySecretPass123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "Password must contain at least one uppercase letter, one lowercase letter and one number")
        @ValidPassword
        String password,
        @Schema(description = "List of consent definition IDs the user accepts during registration", example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
        @NotNull(message = "acceptedConsents is required")
        List<@NotNull(message = "Consent ID cannot be null") UUID> acceptedConsents
) {
}