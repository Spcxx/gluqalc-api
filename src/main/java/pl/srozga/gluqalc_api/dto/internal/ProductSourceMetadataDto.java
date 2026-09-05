package pl.srozga.gluqalc_api.dto.internal;

public record ProductSourceMetadataDto(
        String source,
        String license,
        String licenseUrl,
        String sourceUrl,
        String disclaimer
) {
}
