package pl.srozga.gluqalc_api.component.nutrition;

import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.common.MacroCalculationStrategy;
import pl.srozga.gluqalc_api.common.UserGender;
import pl.srozga.gluqalc_api.dto.internal.UserCalcDataDto;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Component
public class NutritionCalculator {
    public UserCalcDataDto calculate(UserProfile profile) {
        BigDecimal bmr = calculateBMR(profile);

        if (profile.getPhysicalActivityLevel() == null)
            throw new DomainValidationException("Physical activity level is required for TDEE calculation");
        BigDecimal tdee = bmr.multiply(profile.getPhysicalActivityLevel());
        int diff = profile.getKcalGoalDifference() != null ? profile.getKcalGoalDifference() : 0;
        BigDecimal dailyGoalKcal = tdee.add(BigDecimal.valueOf(diff));

        // macro
        if (profile.getMacroStrategy() == null)
            throw new DomainValidationException("Macro calculation strategy is required for macro calculation");

        MacroRatios ratios = getMacroRatios(profile.getMacroStrategy());

        BigDecimal pKcal = dailyGoalKcal.multiply(BigDecimal.valueOf(ratios.protein()));
        BigDecimal fKcal = dailyGoalKcal.multiply(BigDecimal.valueOf(ratios.fat()));
        BigDecimal cKcal = dailyGoalKcal.multiply(BigDecimal.valueOf(ratios.carb()));

        return new UserCalcDataDto(
                bmr.setScale(0, RoundingMode.HALF_UP),
                tdee.setScale(0, RoundingMode.HALF_UP),
                dailyGoalKcal.setScale(0, RoundingMode.HALF_UP),
                cKcal.divide(new BigDecimal("4"), 0, RoundingMode.HALF_UP),
                pKcal.divide(new BigDecimal("4"), 0, RoundingMode.HALF_UP),
                fKcal.divide(new BigDecimal("9"), 0, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal calculateBMR(UserProfile p) {
        if (p.getBirthDate() == null)
            throw new DomainValidationException("Birth date is required for BMR calculation");
        if (p.getBmrMethod() == null)
            throw new DomainValidationException("BMR calculation method is required for BMR calculation");

        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(p.getBirthDate());
        } catch (Exception e) {
            throw new DomainValidationException("Invalid birth date format. Expected YYYY-MM-DD");
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 0 || age > 130)
            throw new DomainValidationException("Invalid age calculated from birth date");

        return switch (p.getBmrMethod()) {
            case KATCH_MCARDLE -> {
                if (p.getBodyFatPercentage() == null)
                    throw new DomainValidationException("Body fat percentage is required for Katch-McArdle BMR calculation");
                if (p.getWeightInKg() == null)
                    throw new DomainValidationException("Weight is required for Katch-McArdle BMR calculation");

                BigDecimal lbm = p.getWeightInKg().multiply(BigDecimal.ONE.subtract(p.getBodyFatPercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));
                yield new BigDecimal("370").add(new BigDecimal("21.6").multiply(lbm));
            }
            case HARRIS_BENEDICT -> {
                if (p.getWeightInKg() == null)
                    throw new DomainValidationException("Weight is required for Harris-Benedict BMR calculation");
                if (p.getHeightInCm() == null)
                    throw new DomainValidationException("Height is required for Harris-Benedict BMR calculation");
                if (p.getGender() == null)
                    throw new DomainValidationException("Gender is required for Harris-Benedict BMR calculation");

                if (p.getGender() == UserGender.MALE) {
                    yield new BigDecimal("66.5").add(new BigDecimal("13.75").multiply(p.getWeightInKg()))
                            .add(new BigDecimal("5.003").multiply(p.getHeightInCm()))
                            .subtract(new BigDecimal("6.75").multiply(new BigDecimal(age)));
                } else {
                    yield new BigDecimal("655.1").add(new BigDecimal("9.563").multiply(p.getWeightInKg()))
                            .add(new BigDecimal("1.85").multiply(p.getHeightInCm()))
                            .subtract(new BigDecimal("4.676").multiply(new BigDecimal(age)));
                }
            }
            default -> {
                if (p.getWeightInKg() == null)
                    throw new DomainValidationException("Weight is required for Mifflin-St Jeor BMR calculation");
                if (p.getHeightInCm() == null)
                    throw new DomainValidationException("Height is required for Mifflin-St Jeor BMR calculation");
                if (p.getGender() == null)
                    throw new DomainValidationException("Gender is required for Mifflin-St Jeor BMR calculation");

                BigDecimal base = p.getWeightInKg().multiply(BigDecimal.TEN)
                        .add(p.getHeightInCm().multiply(new BigDecimal("6.25")))
                        .subtract(new BigDecimal(age * 5));
                yield p.getGender() == UserGender.MALE ? base.add(new BigDecimal("5")) : base.subtract(new BigDecimal("161"));
            }
        };
    }

    public MacroRatios getMacroRatios(MacroCalculationStrategy strategy) {
        return switch (strategy) {
            case BALANCED -> new MacroRatios(0.2, 0.3, 0.5);
            case HIGH_PROTEIN -> new MacroRatios(0.4, 0.25, 0.35);
            case LOW_CARB -> new MacroRatios(0.3, 0.5, 0.2);
            case KETO -> new MacroRatios(0.2, 0.75, 0.05);
        };
    }

    public record MacroRatios(double protein, double fat, double carb) {}
}
