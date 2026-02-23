package pl.srozga.gluqalc_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.srozga.gluqalc_api.dto.internal.ApiError;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PendingConsentsFilter extends OncePerRequestFilter {
    private static final List<String> ALLOWED_PATHS = List.of(
            "/api/v1/auth",
            "/api/v1/consents",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    );

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (ALLOWED_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof AuthUser authUser) {
            if (authUser.consentsPending()) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");

                ApiError apiError = new ApiError(HttpStatus.FORBIDDEN.value(), "You must accept the pending consents to access this resource");
                String jsonResponse = objectMapper.writeValueAsString(apiError);
                response.getWriter().write(jsonResponse);

                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}