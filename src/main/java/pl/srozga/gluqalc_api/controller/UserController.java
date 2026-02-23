package pl.srozga.gluqalc_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.common.UserRole;
import pl.srozga.gluqalc_api.component.rateLimit.RateLimit;
import pl.srozga.gluqalc_api.dto.request.*;
import pl.srozga.gluqalc_api.dto.response.DeviceSessionResponse;
import pl.srozga.gluqalc_api.dto.response.UserAdminResponse;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAdminResponse> getAllUsers() {
        return userService.getAllActiveUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminResponse getUser(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Set<UserRole> getUserRoles(@PathVariable UUID id) {
        return userService.getUserRoles(id);
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addRole(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        userService.addRoleToUser(id, request.role());
    }

    @DeleteMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRole(@PathVariable UUID id, @PathVariable UserRole role) {
        userService.removeRoleFromUser(id, role);
    }

    @GetMapping("/me/sessions")
    @PreAuthorize("isAuthenticated()")
    public List<DeviceSessionResponse> getMySessions(@AuthenticationPrincipal AuthUser authUser) {
        return userService.getUserSessions(authUser.id());
    }

    @DeleteMapping("/me/sessions/{deviceId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeMySession(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable String deviceId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        userService.revokeDeviceSession(authUser.id(), deviceId, authUser, authHeader);
    }

    @GetMapping("/{id}/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DeviceSessionResponse> getUserSessionsByAdmin(@PathVariable UUID id) {
        return userService.getUserSessions(id);
    }

    @DeleteMapping("/{id}/sessions/{deviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeUserSessionByAdmin(
            @PathVariable UUID id,
            @PathVariable String deviceId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        userService.revokeDeviceSession(id, deviceId, authUser, authHeader);
    }

    @RateLimit(maxRequests = 3, timeWindowSeconds = 300)
    @PostMapping("/reset-password/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        userService.initiatePasswordReset(passwordResetRequest.email());
    }

    @RateLimit(maxRequests = 5, timeWindowSeconds = 60)
    @PostMapping("/reset-password/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest passwordResetConfirmRequest) {
        userService.completePasswordReset(passwordResetConfirmRequest);
    }

    @RateLimit(maxRequests = 3, timeWindowSeconds = 300)
    @PostMapping("/change-email/request")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestVerifiedEmailChange(
            @Valid @RequestBody ChangeEmailRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.requestVerifiedUserEmailChange(authUser.id(), request);
    }

    @RateLimit(maxRequests = 5, timeWindowSeconds = 60)
    @PostMapping("/change-email/confirm")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmVerifiedEmailChange(
            @Valid @RequestBody ConfirmEmailChangeRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        userService.confirmVerifiedUserEmailChange(authUser.id(), request);
    }

    @RateLimit(maxRequests = 3, timeWindowSeconds = 300)
    @PostMapping("/me/delete/request")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestMyAccountDeletion(@AuthenticationPrincipal AuthUser authUser) {
        userService.requestUserDeletion(authUser.id());
    }

    @RateLimit(maxRequests = 5, timeWindowSeconds = 60)
    @PostMapping("/me/delete/confirm")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmMyAccountDeletion(
            @Valid @RequestBody ConfirmUserDeletionRequest request,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        userService.confirmUserDeletion(authUser.id(), request.code(), authHeader);
    }
}
