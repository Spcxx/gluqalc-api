package pl.srozga.gluqalc_api.integration.off.dto;

public record OffResponse(
    String code,
    OffProduct product,
    int status
) {
}
