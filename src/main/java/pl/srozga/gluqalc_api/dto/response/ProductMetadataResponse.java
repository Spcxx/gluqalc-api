package pl.srozga.gluqalc_api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductMetadataResponse(
        @JsonProperty("source") String source,
        @JsonProperty("license") String license,
        @JsonProperty("license_url") String licenseUrl,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("disclaimer") String disclaimer
) {
}
