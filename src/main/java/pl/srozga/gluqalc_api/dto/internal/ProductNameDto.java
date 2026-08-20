package pl.srozga.gluqalc_api.dto.internal;

import pl.srozga.gluqalc_api.common.ProductNameSource;
import pl.srozga.gluqalc_api.common.ProductNameType;

import java.io.Serializable;
import java.util.UUID;

public record ProductNameDto(
        UUID id,
        String name,
        String languageCode,
        ProductNameType type,
        ProductNameSource source,
        boolean approved
) implements Serializable {
}
