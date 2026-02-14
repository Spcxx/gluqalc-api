package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(
    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "\\d{6}", message = "Code must be 6 digits")
    String code
) {
}
