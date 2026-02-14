package pl.srozga.gluqalc_api.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;
import pl.srozga.gluqalc_api.exception.TokenAuthenticationException;

@Component
public class JwtAuthenticationConverter implements AuthenticationConverter {
    @Override
    public @Nullable Authentication convert(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || header.isEmpty())
            return null;
        if (!header.startsWith("Bearer "))
            throw new TokenAuthenticationException("Unsupported authorization header format");

        String token = header.substring(7);
        return JwtAuthenticationToken.unauthenticated(token);
    }
}
