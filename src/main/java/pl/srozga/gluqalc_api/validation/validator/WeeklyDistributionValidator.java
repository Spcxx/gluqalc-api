package pl.srozga.gluqalc_api.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.srozga.gluqalc_api.validation.SumZero;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Objects;

public class WeeklyDistributionValidator implements ConstraintValidator<SumZero, Map<DayOfWeek, Integer>> {
    @Override
    public boolean isValid(Map<DayOfWeek, Integer> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty())
            return true;
        int sum = value.values().stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
        return sum == 0;
    }
}
