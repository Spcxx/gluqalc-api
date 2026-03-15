package pl.srozga.gluqalc_api.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.srozga.gluqalc_api.validation.ValidHourlyMap;

import java.math.BigDecimal;
import java.util.Map;

public class HourlyMapValidator implements ConstraintValidator<ValidHourlyMap, Map<Integer, BigDecimal>> {
    @Override
    public boolean isValid(Map<Integer, BigDecimal> value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        if (!value.containsKey(0)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Configuration must include start hour '0'").addConstraintViolation();
            return false;
        }

        for (Map.Entry<Integer, BigDecimal> entry : value.entrySet()) {
            Integer hour = entry.getKey();
            BigDecimal ratio = entry.getValue();

            if (hour < 0 || hour > 23)
                return false;
            if (ratio == null || ratio.compareTo(BigDecimal.ZERO) <= 0)
                return false;
        }

        return true;
    }
}