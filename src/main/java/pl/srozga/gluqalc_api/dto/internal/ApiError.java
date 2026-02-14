package pl.srozga.gluqalc_api.dto.internal;

import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;

import java.time.Instant;

@NullMarked
public record ApiError(
        int status,
        Object message,
        Instant timestamp,
        String traceId
) {
    public ApiError(int status, Object message) {
        this(status, message, Instant.now(), MDC.get("traceId") != null ? MDC.get("traceId") : "n/a");
    }
}
