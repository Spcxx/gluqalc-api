package pl.srozga.gluqalc_api.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.createUser(registerRequest);
    }

    @PostMapping("/refresh")
    public TokenResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request.refreshToken());
    }

    @PostMapping("/google")
    public TokenResponse loginWithGoogleApp(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request.idToken());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        authService.logout(authHeader, authUser.id());
    }

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.code());
    }
}
