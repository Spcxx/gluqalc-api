package pl.srozga.gluqalc_api.component.nutrition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.common.MacroType;
import pl.srozga.gluqalc_api.common.UserGender;
import pl.srozga.gluqalc_api.dto.internal.UserCalcDataDto;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class NutritionCalculator {
    private final ObjectMapper objectMapper;

    public UserCalcDataDto calculate(UserProfile profile) {
        BigDecimal bmr = calculateBMR(profile);

        if (profile.getPhysicalActivityLevel() == null)
            throw new DomainValidationException("Physical activity level is required for TDEE calculation");
        BigDecimal tdee = bmr.multiply(profile.getPhysicalActivityLevel());
        int diff = profile.getKcalGoalDifference() != null ? profile.getKcalGoalDifference() : 0;
        BigDecimal dailyGoalKcal = tdee.add(BigDecimal.valueOf(diff));

        if (profile.getMacroStrategyJson() == null)
            throw new DomainValidationException("Macro calculation strategy is required for macro calculation");

        Map<MacroType, BigDecimal> ratios = getMacroRatios(profile.getMacroStrategyJson());

        BigDecimal pKcal = dailyGoalKcal.multiply(BigDecimal.valueOf(ratios.getOrDefault(MacroType.PROTEIN, BigDecimal.ZERO).doubleValue()));
        BigDecimal fKcal = dailyGoalKcal.multiply(BigDecimal.valueOf(ratios.getOrDefault(MacroType.FAT, BigDecimal.ZERO).doubleValue()));
        BigDecimal cKcal = dailyGoalKcal.multiply(BigDecimal.valueOf(ratios.getOrDefault(MacroType.CARBOHYDRATE, BigDecimal.ZERO).doubleValue()));

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

        try {
            LocalDate.parse(p.getBirthDate());
        } catch (Exception e) {
            throw new DomainValidationException("Invalid birth date format. Expected YYYY-MM-DD");
        }

        int age = p.getAge();
        if (age < 0 || age > 130)
            throw new DomainValidationException("Invalid age calculated from birth date");

        return switch (p.getBmrMethod()) {
            case HARRIS_BENEDICT -> {
                if (p.getWeightInKg() == null)
                    throw new DomainValidationException("Weight is required for Harris-Benedict BMR calculation");
                if (p.getHeightInCm() == null)
                    throw new DomainValidationException("Height is required for Harris-Benedict BMR calculation");
                if (p.getGender() == null)
                    throw new DomainValidationException("Gender is required for Harris-Benedict BMR calculation");
                if (p.getAge() == null)
                    throw new DomainValidationException("Age is required for Harris-Benedict BMR calculation");

                if (p.getGender() == UserGender.MALE) {
                    yield new BigDecimal(13.7516).multiply(p.getWeightInKg())
                            .add(new BigDecimal("5.0033").multiply(p.getHeightInCm()))
                            .subtract(new BigDecimal("6.755").multiply(new BigDecimal(p.getAge())))
                            .add(new BigDecimal("66.473"));
                } else {
                    yield new BigDecimal("9.5634").multiply(p.getWeightInKg())
                            .add(new BigDecimal("1.8496").multiply(p.getHeightInCm()))
                            .subtract(new BigDecimal("4.6756").multiply(new BigDecimal(p.getAge())))
                            .add(new BigDecimal("655.0955"));
                }
            }
            case MIFFLIN_ST_JEOR -> {
                if (p.getWeightInKg() == null)
                    throw new DomainValidationException("Weight is required for Mifflin-St Jeor BMR calculation");
                if (p.getHeightInCm() == null)
                    throw new DomainValidationException("Height is required for Mifflin-St Jeor BMR calculation");
                if (p.getGender() == null)
                    throw new DomainValidationException("Gender is required for Mifflin-St Jeor BMR calculation");
                if (p.getAge() == null)
                    throw new DomainValidationException("Age is required for Mifflin-St Jeor BMR calculation");


                BigDecimal base = p.getWeightInKg().multiply(new BigDecimal("9.99"))
                        .add(p.getHeightInCm().multiply(new BigDecimal("6.25")))
                        .subtract(new BigDecimal(p.getAge()).multiply(new BigDecimal("4.92")));
                yield p.getGender() == UserGender.MALE ? base.add(new BigDecimal("5")) : base.subtract(new BigDecimal("161"));
            }
            case KATCH_MCARDLE -> {
                if (p.getBodyFatPercentage() == null)
                    throw new DomainValidationException("Body fat percentage is required for Katch-McArdle BMR calculation");
                if (p.getWeightInKg() == null)
                    throw new DomainValidationException("Weight is required for Katch-McArdle BMR calculation");

                BigDecimal lbm = p.getWeightInKg().multiply(BigDecimal.ONE.subtract(p.getBodyFatPercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)));
                yield new BigDecimal("370").add(new BigDecimal("21.6").multiply(lbm));
            }
            case OWEN -> {
                if (p.getGender() == null)
                    throw new DomainValidationException("Gender is required for Owen BMR calculation");
                if (p.getAge() == null)
                    throw new DomainValidationException("Age is required for Owen BMR calculation");

                if (p.getGender() == UserGender.MALE) {
                    yield new BigDecimal(10.2).multiply(new BigDecimal(p.getAge())).add(new BigDecimal("879"));
                } else {
                    yield new BigDecimal(7.18).multiply(new BigDecimal(p.getAge())).add(new BigDecimal("795"));
                }
            }
        };
    }

    public Map<MacroType, BigDecimal> getMacroRatios(String  strategy) {
        if (strategy == null || strategy.isEmpty())
            return null;

        try {
            return objectMapper.readValue(
                    strategy,
                    new TypeReference<>() {
                    }
            );
        } catch (JsonProcessingException e) {
            log.error("Error deserializing macro strategy", e);
            return null;
        }
    }
}
