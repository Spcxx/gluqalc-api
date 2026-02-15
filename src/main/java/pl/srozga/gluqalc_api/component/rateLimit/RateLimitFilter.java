package pl.srozga.gluqalc_api.component.rateLimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.srozga.gluqalc_api.dto.internal.ApiError;
import pl.srozga.gluqalc_api.utils.IpResolver;

import java.io.IOException;

@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final RateLimitService rateLimitService;

    private static final String HEADER_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    private static final String HEADER_RETRY_AFTER = "Retry-After";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String ip = IpResolver.getClientIp(request);
        RateLimitService.RateLimitResponse result = rateLimitService.checkRateLimit(ip);

        if (result.allowed()) {
            response.addHeader(HEADER_LIMIT_REMAINING, String.valueOf(result.remainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            response.addHeader(HEADER_RETRY_AFTER, String.valueOf(result.retryAfterSeconds()));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiError apiError = new ApiError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many requests. Try again later. Retry after " + result.retryAfterSeconds() + " s.");
            String jsonResponse = objectMapper.writeValueAsString(apiError);
            response.getWriter().write(jsonResponse);
        }
    }
}
