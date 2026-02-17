package pl.srozga.gluqalc_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.srozga.gluqalc_api.common.InsulinFatProteinStrategy;
import pl.srozga.gluqalc_api.dto.request.UpdateUserProfileRequest;

public class DiabetesProfileValidator implements ConstraintValidator<ValidDiabetesProfile, UpdateUserProfileRequest> {
    @Override
    public boolean isValid(UpdateUserProfileRequest request, ConstraintValidatorContext context) {
        if (request == null)
            return true;

        if (request.ifpStrategy() != null && request.ifpStrategy() != InsulinFatProteinStrategy.NONE) {
            if (request.insulinFatProteinRatio() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Insulin Fat-Protein Ratio is required for the selected strategy")
                        .addPropertyNode("insulinFatProteinRatio")
                        .addConstraintViolation();
                return false;
            } else if (request.insulinSensitivityFactor() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Insulin Sensitivity Factor is required for the selected strategy")
                        .addPropertyNode("insulinSensitivityFactor")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}