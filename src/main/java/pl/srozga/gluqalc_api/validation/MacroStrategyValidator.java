package pl.srozga.gluqalc_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.srozga.gluqalc_api.common.MacroType;

import java.math.BigDecimal;
import java.util.Map;

public class MacroStrategyValidator implements ConstraintValidator<ValidMacroStrategy, Map<MacroType, BigDecimal>> {
    @Override
    public boolean isValid(Map<MacroType, BigDecimal> value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        BigDecimal sum = BigDecimal.ZERO;

        for (MacroType macroType : MacroType.values()) {
            if (!value.containsKey(macroType)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Missing required macro type: " + macroType)
                        .addConstraintViolation();
                return false;
            }

            BigDecimal ratio = value.get(macroType);

            if (ratio == null || ratio.compareTo(BigDecimal.ZERO) < 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Value for " + macroType + " cannot be null or negative")
                        .addConstraintViolation();
                return false;
            }

            sum = sum.add(ratio);
        }

        if (sum.compareTo(BigDecimal.ONE) != 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The sum of macro ratios must be exactly 1.0")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
