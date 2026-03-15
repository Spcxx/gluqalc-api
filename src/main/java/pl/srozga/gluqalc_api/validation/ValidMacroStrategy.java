package pl.srozga.gluqalc_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.srozga.gluqalc_api.validation.validator.MacroStrategyValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MacroStrategyValidator.class)
@Documented
public @interface ValidMacroStrategy {
    String message() default "Macro strategy map must contain all macro types with non-negative values.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
