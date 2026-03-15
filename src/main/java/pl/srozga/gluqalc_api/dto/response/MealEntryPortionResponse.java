package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Portion details used for a specific meal entry")
public record MealEntryPortionResponse(
        @Schema(description = "ID of the portion definition", example = "987fcdeb-51a2-43d7-9012-3456789abcde")
        UUID id,
        @Schema(description = "Name of the portion", example = "slice")
        String name,
        @Schema(description = "The quantity of units consumed", example = "2.0")
        BigDecimal quantity,
        @Schema(description = "The weight of a single unit in grams", example = "30.0")
        BigDecimal unitWeight,
        @Schema(description = "Calculated total weight in grams", example = "60.0", nullable = true)
        @Nullable BigDecimal totalWeight
) {
}