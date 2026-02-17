package pl.srozga.gluqalc_api.integration.off.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffNutriments(
        @JsonProperty("energy-kcal_100g") BigDecimal energyKcal,
        @JsonProperty("carbohydrates_100g") BigDecimal carbohydrates,
        @JsonProperty("sugars_100g") BigDecimal sugars,
        @JsonProperty("fat_100g") BigDecimal fat,
        @JsonProperty("saturated-fat_100g") BigDecimal saturatedFat,
        @JsonProperty("proteins_100g") BigDecimal protein,
        @JsonProperty("fiber_100g") BigDecimal fiber,
        @JsonProperty("salt_100g") BigDecimal salt
) {
}
