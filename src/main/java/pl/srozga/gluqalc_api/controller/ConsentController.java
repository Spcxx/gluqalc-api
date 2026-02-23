package pl.srozga.gluqalc_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.dto.request.AcceptConsentsRequest;
import pl.srozga.gluqalc_api.dto.response.ConsentResponse;
import pl.srozga.gluqalc_api.dto.response.TokenResponse;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.AuthService;
import pl.srozga.gluqalc_api.service.ConsentService;
import pl.srozga.gluqalc_api.utils.IpResolver;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
public class ConsentController {
    private final ConsentService consentService;
    private final AuthService authService;

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public List<ConsentResponse> getPendingConsents(@AuthenticationPrincipal AuthUser authUser) {
        return consentService.getPendingConsentsForUser(authUser.id());
    }

    @PostMapping("/accept")
    @PreAuthorize("isAuthenticated()")
    public TokenResponse acceptConsents(
            @Valid @RequestBody AcceptConsentsRequest request,
            @AuthenticationPrincipal AuthUser authUser,
            HttpServletRequest httpRequest
    ) {
        consentService.acceptConsents(authUser.id(), request, IpResolver.getClientIp(httpRequest));

        return authService.refreshToken(
                request.refreshToken(),
                IpResolver.getClientIp(httpRequest),
                httpRequest.getHeader(HttpHeaders.USER_AGENT)
        );
    }
}