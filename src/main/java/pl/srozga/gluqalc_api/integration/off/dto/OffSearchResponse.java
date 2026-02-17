package pl.srozga.gluqalc_api.integration.off.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OffSearchResponse(
        Integer count,
        Integer page,
        List<OffProduct> products
) {
}
