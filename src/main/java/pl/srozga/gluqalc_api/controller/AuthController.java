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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.component.rateLimit.RateLimit;
import pl.srozga.gluqalc_api.dto.request.*;
import pl.srozga.gluqalc_api.dto.response.TokenResponse;
import pl.srozga.gluqalc_api.dto.response.UserResponse;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.AuthService;
import pl.srozga.gluqalc_api.service.UserService;
import pl.srozga.gluqalc_api.utils.IpResolver;

@Tag(name = "01. Authentication", description = "Endpoints for managing user sessions, registration and authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "Standard login", description = "Authenticates user with email and password.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials, account not verified or registered via external provider"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @RateLimit(maxRequests = 10, timeWindowSeconds = 300)
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        return authService.login(loginRequest, IpResolver.getClientIp(request), request.getHeader(HttpHeaders.USER_AGENT));
    }

    @Operation(summary = "User registration", description = "Registers a new user account.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Conflict: User with this email already exists"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    @RateLimit(maxRequests = 10, timeWindowSeconds = 300)
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.createUser(registerRequest);
    }

    @Operation(summary = "Refresh token", description = "Generates a new pair of tokens based on a valid Refresh Token.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens successfully refreshed"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid, expired or revoked Refresh Token")
    })
    @PostMapping("/refresh")
    public TokenResponse refreshToken(@Valid @RequestBody RefreshTokenRequest requestBody, HttpServletRequest request) {
        return authService.refreshToken(requestBody.refreshToken(), IpResolver.getClientIp(request), request.getHeader(HttpHeaders.USER_AGENT));
    }

    @Operation(summary = "Google login", description = "Authenticates user via Google OAuth2 ID Token. Registers the user if they do not exist.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid Google ID Token or email registered via different provider"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @PostMapping("/google")
    @RateLimit(maxRequests = 10, timeWindowSeconds = 300)
    public TokenResponse loginWithGoogleApp(@Valid @RequestBody GoogleLoginRequest requestBody, HttpServletRequest request) {
        return authService.loginWithGoogle(requestBody.idToken(), requestBody.deviceId(), IpResolver.getClientIp(request), request.getHeader(HttpHeaders.USER_AGENT));
    }

    @Operation(summary = "Logout", description = "Invalidates the current session and the provided Refresh Token.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Successfully logged out"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT)")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody RefreshTokenRequest requestBody
    ) {
        authService.logout(authHeader, requestBody.refreshToken());
    }

    @Operation(summary = "Verify email", description = "Verifies the user account using the token sent via email.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email successfully verified"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired verification code"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @PostMapping("/verify")
    @RateLimit(maxRequests = 10, timeWindowSeconds = 300)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.code());
    }

    @Operation(summary = "Link local account", description = "Sets a password for an account originally created via Google, enabling standard login.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Local account successfully linked"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Conflict: Password is already set for this account")
    })
    @PostMapping("/link/local")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void linkLocalAccount(@Valid @RequestBody SetPasswordRequest request, @AuthenticationPrincipal AuthUser authUser) {
        authService.linkLocalAccount(request.password(), authUser.id());
    }

    @Operation(summary = "Link Google account", description = "Links a Google account to the currently authenticated local user account.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Google account successfully linked"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized or Google email does not match profile email"),
            @ApiResponse(responseCode = "409", description = "Conflict: Google account is already linked")
    })
    @PostMapping("/link/google")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void linkGoogleAccount(@Valid @RequestBody GoogleLoginRequest request, @AuthenticationPrincipal AuthUser authUser) {
        authService.linkGoogleAccount(request.idToken(), authUser.id());
    }

    @Operation(summary = "Change unverified email", description = "Allows a user to correct their email address if they provided wrong email address (only works before the account is verified).")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email successfully changed and new verification email sent"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid current email or password"),
            @ApiResponse(responseCode = "409", description = "Conflict: Account is already verified, new email is the same or email is already taken")
    })
    @PostMapping("/change-email")
    @PreAuthorize("isAnonymous()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeUnverifiedEmail(@Valid @RequestBody ChangeUnverifiedEmailRequest request) {
        userService.changeUnverifiedUserEmail(request);
    }
}
