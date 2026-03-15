package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Calculated insulin dose requirements and diabetic units for a specific meal or product")
public record InsulinDoseResponse(
        @Schema(description = "Carbohydrate Units. 1 unit equals 10g of net carbohydrates.", example = "2.5")
        BigDecimal carbUnit,
        @Schema(description = "Fat-Protein Units. 1 unit equals 100 kcal from fat and protein.", example = "1.2")
        BigDecimal fatProteinUnit,
        @Schema(description = "Calculated insulin dose for the carbohydrate content based on user's current ICR", example = "3.0")
        BigDecimal carbDose,
        @Schema(description = "Calculated insulin dose for the fat and protein content based on user's current IFP ratio", example = "1.8")
        BigDecimal fatProteinDose,
        @Schema(description = "Total calculated insulin dose (sum of carbohydrate and fat-protein doses)", example = "4.8")
        BigDecimal totalDose,
        @Schema(description = "Recommended duration for an extended or multi-wave bolus based on glycemic index and fat content", example = "120")
        Integer bolusDurationMinutes,
        @Schema(description = "Additional information provided by the system regarding the insulin dose calculation")
        String description
) {
}