package pl.srozga.gluqalc_api.component.meal;

import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.dto.response.*;
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


        ProductNutritionResponse totalNutrition = calculateTotalNutrition(entryResponses);

        return new MealCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSortOrder(),
                totalNutrition,
                entryResponses
        );
    }

    private ProductNutritionResponse calculateTotalNutrition(List<MealEntryResponse> entries) {
        BigDecimal accEnergy = BigDecimal.ZERO;
        BigDecimal accCarbs = BigDecimal.ZERO;
        BigDecimal accSugars = BigDecimal.ZERO;
        BigDecimal accFat = BigDecimal.ZERO;
        BigDecimal accSatFat = BigDecimal.ZERO;
        BigDecimal accProtein = BigDecimal.ZERO;
        BigDecimal accFiber = BigDecimal.ZERO;
        BigDecimal accSalt = BigDecimal.ZERO;

        for (MealEntryResponse entry : entries) {
            MealEntryNutritionResponse n = entry.nutrition();
            if (n != null) {
                accEnergy = addSafe(accEnergy, n.energyKcal());
                accCarbs = addSafe(accCarbs, n.carbohydrates());
                accSugars = addSafe(accSugars, n.sugars());
                accFat = addSafe(accFat, n.fat());
                accSatFat = addSafe(accSatFat, n.saturatedFat());
                accProtein = addSafe(accProtein, n.protein());
                accFiber = addSafe(accFiber, n.fiber());
                accSalt = addSafe(accSalt, n.salt());
            }
        }

        return new ProductNutritionResponse(
                accEnergy,
                accCarbs,
                accSugars,
                accFat,
                accSatFat,
                accProtein,
                accFiber,
                accSalt,
                null
        );
    }

    private BigDecimal addSafe(BigDecimal accumulator, BigDecimal value) {
        return accumulator.add(value != null ? value : BigDecimal.ZERO);
    }
}
