package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeUnverifiedEmailRequest(
        @NotBlank(message = "Old email is required")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
        @Email
        String oldEmail,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        @NotBlank(message = "New email is required")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
        @Email
        String newEmail
) {
}
