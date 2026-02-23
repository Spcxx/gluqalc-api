package pl.srozga.gluqalc_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import pl.srozga.gluqalc_api.component.logging.MdcFilter;
import pl.srozga.gluqalc_api.component.rateLimit.RateLimitFilter;
import pl.srozga.gluqalc_api.component.rateLimit.RateLimitService;
import pl.srozga.gluqalc_api.security.PendingConsentsFilter;

@Configuration
public class FilterConfig {
    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        return new RateLimitFilter(objectMapper, rateLimitService);
    }

    @Bean
    public MdcFilter mdcFilter() {
        return new MdcFilter();
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<MdcFilter> mdcFilterRegistration(MdcFilter filter) {
        FilterRegistrationBean<MdcFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<PendingConsentsFilter> pendingConsentsFilterRegistration(PendingConsentsFilter filter) {
        FilterRegistrationBean<PendingConsentsFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }
}
