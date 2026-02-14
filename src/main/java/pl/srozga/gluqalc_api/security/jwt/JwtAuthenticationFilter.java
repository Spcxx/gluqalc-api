package pl.srozga.gluqalc_api.security.jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFilter;

public class JwtAuthenticationFilter extends AuthenticationFilter {
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtAuthenticationConverter jwtAuthenticationConverter, AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationManager, jwtAuthenticationConverter);
        setSuccessHandler(((_, _, _) -> {}));
        setFailureHandler(authenticationEntryPoint::commence);
    }
}
