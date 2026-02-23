package pl.srozga.gluqalc_api.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import pl.srozga.gluqalc_api.component.logging.MdcFilter;
import pl.srozga.gluqalc_api.security.PendingConsentsFilter;
import pl.srozga.gluqalc_api.security.SecurityExceptionHandler;
import pl.srozga.gluqalc_api.security.jwt.JwtAuthenticationConfigurer;
import pl.srozga.gluqalc_api.security.jwt.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final SecurityExceptionHandler securityExceptionHandler;
    private final JwtAuthenticationConfigurer jwtAuthenticationConfigurer;

    @Bean
    public DefaultAuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();

        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(false);

        configuration.setAllowedHeaders(java.util.List.of(
                "Authorization",
                "Content-Type",
                "X-Trace-Id"
        ));

        configuration.setExposedHeaders(java.util.List.of(
                "X-Rate-Limit-Remaining",
                "Retry-After",
                "X-Trace-Id"
        ));


        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MdcFilter mdcFilter, PendingConsentsFilter pendingConsentsFilter) {
        http
                .cors(Customizer.withDefaults())
                .addFilterBefore(mdcFilter, LogoutFilter.class)
                .with(jwtAuthenticationConfigurer, Customizer.withDefaults())
                .addFilterAfter(pendingConsentsFilter, JwtAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        matcher -> matcher
                                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html"
                                ).permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(customizer -> customizer
                        .authenticationEntryPoint(securityExceptionHandler)
                        .accessDeniedHandler(securityExceptionHandler)
                );
        return http.build();
    }
}
