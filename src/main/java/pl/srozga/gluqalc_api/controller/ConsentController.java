package pl.srozga.gluqalc_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "02. Consents", description = "Endpoints for managing legal consents and privacy policies")
@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
public class ConsentController {
    private final ConsentService consentService;
    private final AuthService authService;

    @Operation(summary = "Get all active consents", description = "Retrieves the publicly available consent definitions for display in the registration and consent flow.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active consent definitions")
    })
    @GetMapping
    public List<ConsentResponse> getAllConsents() {
        return consentService.getAllActiveConsents();
    }

    @Operation(summary = "Get pending consents", description = "Retrieves a list of active and required consents that the user has not yet accepted.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending consents"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT)")
    })
    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public List<ConsentResponse> getPendingConsents(@AuthenticationPrincipal AuthUser authUser) {
        return consentService.getPendingConsentsForUser(authUser.id());
    }

    @Operation(summary = "Accept consents", description = "Saves the user's acceptance of the specified consents and refreshes the JWT token to update claims.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consents successfully accepted and tokens refreshed"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., malformed request body)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Missing/invalid JWT) or invalid Refresh Token"),
            @ApiResponse(responseCode = "404", description = "Not found: User not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Some of the provided consents are invalid or inactive")
    })
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