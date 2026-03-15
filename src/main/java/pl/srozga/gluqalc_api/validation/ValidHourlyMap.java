package pl.srozga.gluqalc_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.srozga.gluqalc_api.validation.validator.HourlyMapValidator;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HourlyMapValidator.class)
@Documented
public @interface ValidHourlyMap {
    String message() default "Map keys must be integers 0-23 and values must be positive. Hour 0 is required.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
