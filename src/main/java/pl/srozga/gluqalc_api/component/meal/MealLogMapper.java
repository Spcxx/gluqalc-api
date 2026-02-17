package pl.srozga.gluqalc_api.component.meal;

import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.dto.response.MealCategoryResponse;
import pl.srozga.gluqalc_api.dto.response.MealEntryNutritionResponse;
import pl.srozga.gluqalc_api.dto.response.MealEntryPortionResponse;
import pl.srozga.gluqalc_api.dto.response.MealEntryResponse;
import pl.srozga.gluqalc_api.entity.MealCategory;
import pl.srozga.gluqalc_api.entity.MealEntry;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MealLogMapper {
    public MealEntryResponse toDto(MealEntry entry) {
        return new MealEntryResponse(
                entry.getId(),
                entry.getProductId(),
                entry.getProductName(),
                entry.getBrand(),
                entry.getBarcode(),
                new MealEntryPortionResponse(
                        entry.getPortionId(),
                        entry.getPortionName(),
                        entry.getPortionQuantity(),
                        entry.getPortionUnitWeight(),
                        entry.getWeightInGrams()
                ),
                new MealEntryNutritionResponse(
                        entry.getEnergyKcal(),
                        entry.getCarbohydrates(),
                        entry.getSugars(),
                        entry.getFat(),
                        entry.getSaturatedFat(),
                        entry.getProtein(),
                        entry.getFiber(),
                        entry.getSalt(),
                        entry.getGlycemicIndex()
                )
        );
    }

    public MealCategoryResponse toDto(MealCategory category, List<MealEntry> entries) {
        List<MealEntryResponse> entryResponses = entries.stream()
                .map(this::toDto)
                .toList();


        BigDecimal totalEnergyKcal = entryResponses.stream()
                .map(e -> e.nutrition().energyKcal())
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MealCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSortOrder(),
                totalEnergyKcal,
                entryResponses
        );
    }
}
