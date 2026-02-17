package pl.srozga.gluqalc_api.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import pl.srozga.gluqalc_api.common.UserGender;
import pl.srozga.gluqalc_api.validation.SumZero;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

public record UpdateUserProfileRequest(
        UserGender gender,

        @Positive(message = "Weight must be positive")
        BigDecimal weightInKg,

        @Positive(message = "Height must be positive")
        BigDecimal heightInCm,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate,

        @DecimalMin(value = "1.0", message = "Activity level too low")
        @DecimalMax(value = "3.0", message = "Activity level too high")
        BigDecimal physicalActivityLevel,

        Integer kcalGoalDifference,
        @SumZero
        Map<DayOfWeek, Integer> weeklyKcalDistribution
) {}
