package pl.srozga.gluqalc_api.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SecurityEventsListener {
    @EventListener
    public void handleSuccess(AuthenticationSuccessEvent event) {
        log.info("Authentication successful for user: {}", event.getAuthentication().getName());
    }

    @EventListener
    public void handleFailure(org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent event) {
        log.warn("Authentication failed for user: {} | Reason: {}", event.getAuthentication().getName(), event.getException().getMessage());
    }
}
