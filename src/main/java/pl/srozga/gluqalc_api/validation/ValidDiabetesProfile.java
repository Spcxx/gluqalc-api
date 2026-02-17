package pl.srozga.gluqalc_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DiabetesProfileValidator.class)
@Documented
public @interface ValidDiabetesProfile {
    String message() default "Diabetes configuration is invalid. Ratios are required when strategy is active.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
