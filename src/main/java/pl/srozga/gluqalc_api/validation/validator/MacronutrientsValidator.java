package pl.srozga.gluqalc_api.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.srozga.gluqalc_api.validation.ValidMacronutrients;
import pl.srozga.gluqalc_api.validation.interfaces.MacronutrientsData;

public class MacronutrientsValidator implements ConstraintValidator<ValidMacronutrients, MacronutrientsData> {
    @Override
    public boolean isValid(MacronutrientsData request, ConstraintValidatorContext context) {
        if (request == null)
            return true;

        boolean isValid = true;

        context.disableDefaultConstraintViolation();

        if (request.sugars() != null && request.carbohydrates() != null) {
            if (request.sugars().compareTo(request.carbohydrates()) > 0) {
                addFieldError(context, "sugars", "Sugars cannot be greater than total carbohydrates");
                isValid = false;
            }
        }

        if (request.saturatedFat() != null && request.fat() != null) {
            if (request.saturatedFat().compareTo(request.fat()) > 0) {
                addFieldError(context, "saturatedFat", "Saturated fat cannot be greater than total fat");
                isValid = false;
            }
        }

        return isValid;
    }

    private void addFieldError(ConstraintValidatorContext context, String fieldName, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(fieldName)
                .addConstraintViolation();
    }
}
