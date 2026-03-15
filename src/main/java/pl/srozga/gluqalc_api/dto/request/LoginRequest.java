package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Size(min = 5, max = 255, message = "Email must be between 5 and 255 characters")
    @Email
    String email,
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    String password,
    @Nullable
    @Size(max = 255, message = "Device ID must be at most 255 characters")
    String deviceId
) {
}
