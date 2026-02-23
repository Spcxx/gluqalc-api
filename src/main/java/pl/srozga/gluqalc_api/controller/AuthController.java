package pl.srozga.gluqalc_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.dto.request.*;
import pl.srozga.gluqalc_api.dto.response.TokenResponse;
import pl.srozga.gluqalc_api.dto.response.UserResponse;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.AuthService;
import pl.srozga.gluqalc_api.service.UserService;
import pl.srozga.gluqalc_api.utils.IpResolver;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        return authService.login(loginRequest, IpResolver.getClientIp(request), request.getHeader(HttpHeaders.USER_AGENT));
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.createUser(registerRequest);
    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(@Valid @RequestBody RefreshTokenRequest requestBody, HttpServletRequest request) {
        return authService.refreshToken(requestBody.refreshToken(), IpResolver.getClientIp(request), request.getHeader(HttpHeaders.USER_AGENT));
    }

    @PostMapping("/google")
    public TokenResponse loginWithGoogleApp(@Valid @RequestBody GoogleLoginRequest requestBody, HttpServletRequest request) {
        return authService.loginWithGoogle(requestBody.idToken(), requestBody.deviceId(), IpResolver.getClientIp(request), request.getHeader(HttpHeaders.USER_AGENT));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody RefreshTokenRequest requestBody
    ) {
        authService.logout(authHeader, requestBody.refreshToken());
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.code());
    }

    @PostMapping("/link/local")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void linkLocalAccount(@Valid @RequestBody SetPasswordRequest request, @AuthenticationPrincipal AuthUser authUser) {
        authService.linkLocalAccount(request.password(), authUser.id());
    }

    @PostMapping("/link/google")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void linkGoogleAccount(@Valid @RequestBody GoogleLoginRequest request, @AuthenticationPrincipal AuthUser authUser) {
        authService.linkGoogleAccount(request.idToken(), authUser.id());
    }

    @PostMapping("/change-email")
    @PreAuthorize("isAnonymous()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeUnverifiedEmail(@Valid @RequestBody ChangeUnverifiedEmailRequest request) {
        userService.changeUnverifiedUserEmail(request);
    }
}
