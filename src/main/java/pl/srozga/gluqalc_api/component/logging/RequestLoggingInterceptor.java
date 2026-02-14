package pl.srozga.gluqalc_api.component.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

@Component
@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final String START_TIME_ATTR_NAME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        request.setAttribute(START_TIME_ATTR_NAME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
        Object startTimeObject = request.getAttribute(START_TIME_ATTR_NAME);
        long startTime = Objects.nonNull(startTimeObject) ? (long) startTimeObject : System.currentTimeMillis();
        long duration = System.currentTimeMillis() - startTime;

        if (ex != null) {
            log.error("{} -> {} | STATUS: {} | TIME: {} ms | IP: {} | EXCEPTION: {}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration, request.getRemoteAddr(), ex.getMessage(), ex);
        } else {
            log.info("{} -> {} | STATUS: {} | TIME: {} ms | IP: {}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration, request.getRemoteAddr());
        }
    }
}
