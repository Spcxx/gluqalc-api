package pl.srozga.gluqalc_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.srozga.gluqalc_api.validation.validator.MacronutrientsValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MacronutrientsValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMacronutrients {
    String message() default "Invalid macronutrients composition";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
