package pl.srozga.gluqalc_api.dto.response;

import java.util.UUID;

public record ConsentResponse(
        UUID id,
        String code,
        String description,
        String version,
        boolean required
) {
}
