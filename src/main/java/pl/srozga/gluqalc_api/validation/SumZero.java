package pl.srozga.gluqalc_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.srozga.gluqalc_api.validation.validator.WeeklyDistributionValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WeeklyDistributionValidator.class)
@Documented
public @interface SumZero {
    String message() default "The sum of the distribution must be exactly zero.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
