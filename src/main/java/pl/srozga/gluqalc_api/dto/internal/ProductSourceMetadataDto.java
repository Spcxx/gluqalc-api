package pl.srozga.gluqalc_api.dto.internal;

import java.io.Serializable;

public record ProductSourceMetadataDto(
        String source,
        String license,
        String licenseUrl,
        String sourceUrl,
        String disclaimer
) implements Serializable {
}
