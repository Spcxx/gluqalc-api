package pl.srozga.gluqalc_api.component.diabetes;

import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.dto.internal.DiabetesCalcDataDto;
import pl.srozga.gluqalc_api.dto.response.InsulinDoseResponse;

@Component
public class DiabetesMapper {
    public InsulinDoseResponse toResponse(DiabetesCalcDataDto dto) {
        return new InsulinDoseResponse(
                dto.carbUnit(),
                dto.fatProteinUnit(),
                dto.carbDose(),
                dto.fatProteinDose(),
                dto.totalDose(),
                dto.bolusDurationMinutes(),
                dto.description()
        );
    }
}
