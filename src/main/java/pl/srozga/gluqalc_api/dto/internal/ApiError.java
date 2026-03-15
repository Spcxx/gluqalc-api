package pl.srozga.gluqalc_api.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;

import java.time.Instant;

@NullMarked
@Schema(description = "Standard API error response")
public record ApiError(
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "Error message, which can be a simple string or a map of field validation errors", example = "Validation failed for request")
        Object message,
        @Schema(description = "Timestamp of when the error occurred (in UTC)", example = "2026-03-15T10:15:30Z")
        Instant timestamp,
        @Schema(description = "Unique trace ID (UUID format) for tracking the request in logs", example = "550e8400-e29b-41d4-a716-446655440000")
        String traceId
) {
    public ApiError(int status, Object message) {
        this(status, message, Instant.now(), MDC.get("traceId") != null ? MDC.get("traceId") : "n/a");
    }
}
