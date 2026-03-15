package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Represents a user-proposed modification to an existing product portion")
public record PortionChangeResponse(
        @Schema(description = "Unique identifier of the change proposal", example = "a1b2c3d4-e5f6-7g8h-9i0j-1234567890ab")
        UUID id,
        @Schema(description = "ID of the portion that this change refers to", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID portionId,
        @Schema(description = "ID of the user who proposed the change", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        UUID userId,
        @Schema(description = "Proposed new name for the portion", example = "Large Bowl")
        String name,
        @Schema(description = "Proposed new weight in grams", example = "450.00")
        BigDecimal weightInGrams
) {
}