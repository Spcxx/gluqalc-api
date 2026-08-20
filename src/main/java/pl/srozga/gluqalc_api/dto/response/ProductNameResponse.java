package pl.srozga.gluqalc_api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.srozga.gluqalc_api.common.ProductNameSource;
import pl.srozga.gluqalc_api.common.ProductNameType;

import java.util.UUID;

@Schema(description = "Translated or alternative product name")
public record ProductNameResponse(
        UUID id,
        String name,
        String languageCode,
        ProductNameType type,
        ProductNameSource source,
        boolean approved
) {
}
