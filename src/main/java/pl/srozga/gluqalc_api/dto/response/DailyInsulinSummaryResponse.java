package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Summary of insulin doses and diabetic units calculated and estimated for a specific day")
public record DailyInsulinSummaryResponse(
        @Schema(description = "Total Carbohydrate Units (CU) consumed today. 1 unit equals 10g of net carbohydrates.", example = "12.5")
        BigDecimal totalCarbUnits,
        @Schema(description = "Total Fat-Protein Units (FPU) consumed today. 1 unit equals 100 kcal from fat and protein.", example = "4.2")
        BigDecimal totalFatProteinUnits,
        @Schema(description = "Total insulin dose calculated for carbohydrates consumed today", example = "15.0")
        BigDecimal totalCarbDose,
        @Schema(description = "Total insulin dose calculated for fat and protein consumed today", example = "6.5")
        BigDecimal totalFatProteinDose,
        @Schema(description = "Total sum of all mealtime bolus doses calculated for today (sum of carbohydrate and fat-protein doses)", example = "21.5")
        BigDecimal totalBolusDose,
        @Schema(description = "Estimated Total Daily Dose (TDD) based on body weight and user's configured multiplier. May be null if profile data is incomplete.", example = "44.0")
        BigDecimal estimatedTotalDailyDose,
        @Schema(description = "User's configured total daily basal insulin (sum of 24h pump profile or long-acting pen injections)", example = "20.0")
        BigDecimal dailyBasalInsulin,
        @Schema(description = "Estimated total bolus target for the day (TDD minus daily basal). Null if TDD or basal configuration is missing.", example = "24.0")
        BigDecimal estimatedBolusTarget,
        @Schema(description = "Remaining estimated bolus dose for the day (estimatedBolusTarget minus consumedBolusDose)", example = "2.5")
        BigDecimal remainingBolusTarget
) {
}