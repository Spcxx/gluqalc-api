package pl.srozga.gluqalc_api.component.rateLimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import pl.srozga.gluqalc_api.dto.internal.ApiError;
import pl.srozga.gluqalc_api.utils.IpResolver;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;
    private final RateLimitService rateLimitService;

    private static final String HEADER_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    private static final String HEADER_RETRY_AFTER = "Retry-After";

    @Value("${app.rate-limiter.max-requests}")
    private int maxRequests;
    @Value("${app.rate-limiter.time-window}")
    private int timeWindowSeconds;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

            int maxRequests = this.maxRequests;
            int timeWindowSeconds = this.timeWindowSeconds;
            if (rateLimit != null) {
                maxRequests = rateLimit.maxRequests() != -1 ? rateLimit.maxRequests() : maxRequests;
                timeWindowSeconds = rateLimit.timeWindowSeconds() != -1 ? rateLimit.timeWindowSeconds() : timeWindowSeconds;
            }

            String ip = IpResolver.getClientIp(request);
            String actionKey = handlerMethod.getBeanType().getSimpleName() + ":" + handlerMethod.getMethod().getName();

            RateLimitService.RateLimitResponse result = rateLimitService.checkRateLimit(ip, actionKey, maxRequests, timeWindowSeconds);
            if (!result.allowed()) {
                response.addHeader(HEADER_RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                ApiError apiError = new ApiError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests. Try again later. Retry after " + result.retryAfterSeconds() + " s.");
                String jsonResponse = objectMapper.writeValueAsString(apiError);
                response.getWriter().write(jsonResponse);

                return false;
            } else {
                response.addHeader(HEADER_LIMIT_REMAINING, String.valueOf(result.remainingTokens()));
            }
        }
        return true;
    }
}
