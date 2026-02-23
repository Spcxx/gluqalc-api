package pl.srozga.gluqalc_api.component.meal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.component.diabetes.DiabetesCalculator;
import pl.srozga.gluqalc_api.component.diabetes.DiabetesMapper;
import pl.srozga.gluqalc_api.dto.internal.DiabetesCalcDataDto;
import pl.srozga.gluqalc_api.dto.response.*;
import pl.srozga.gluqalc_api.entity.MealCategory;
import pl.srozga.gluqalc_api.entity.MealEntry;
import pl.srozga.gluqalc_api.entity.UserProfile;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MealLogMapper {
    private final DiabetesMapper diabetesMapper;
    private final DiabetesCalculator diabetesCalculator;

    public MealEntryResponse toDto(MealEntry entry, DiabetesCalcDataDto calcData) {
        InsulinDoseResponse insulinDose = null;
        if (calcData != null)
            insulinDose = diabetesMapper.toResponse(calcData);

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
                ),
                insulinDose,
                entry.getConsumedAt(),
                entry.getConsumedAtTime()
        );
    }

    public MealCategoryResponse toCategoryDto(MealCategory category, List<MealEntryResponse> entryResponses, UserProfile profile, LocalTime time) {
        ProductNutritionResponse totalNutrition = calculateTotalNutrition(entryResponses);
        DiabetesCalcDataDto totalInsulinDoseCalc = diabetesCalculator.calculateForCategory(entryResponses, profile, time);
        InsulinDoseResponse totalInsulinDose = null;
        if (totalInsulinDoseCalc != null)
            totalInsulinDose = diabetesMapper.toResponse(totalInsulinDoseCalc);

        return new MealCategoryResponse(
                category.getId(),
                category.getName(),
                category.getSortOrder(),
                totalNutrition,
                entryResponses,
                totalInsulinDose
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

        BigDecimal averageGiBg = diabetesCalculator.calculateAverageGlycemicIndex(entries);
        Integer averageGi = averageGiBg != null ? averageGiBg.intValue() : null;

        return new ProductNutritionResponse(
                accEnergy,
                accCarbs,
                accSugars,
                accFat,
                accSatFat,
                accProtein,
                accFiber,
                accSalt,
                averageGi
        );
    }

    private BigDecimal addSafe(BigDecimal accumulator, BigDecimal value) {
        return accumulator.add(value != null ? value : BigDecimal.ZERO);
    }
}
