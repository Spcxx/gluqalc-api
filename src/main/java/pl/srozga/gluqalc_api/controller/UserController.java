package pl.srozga.gluqalc_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "03. Users & Account", description = "Endpoints for account management, security settings, session tracking and admin user administration")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get all users (Admin)", description = "Retrieves a list of all active (non-deleted) users in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserAdminResponse> getAllUsers() {
        return userService.getAllActiveUsers();
    }

    @Operation(summary = "Get user details (Admin)", description = "Retrieves detailed information about a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user details"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found: User does not exist")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminResponse getUser(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Delete user (Admin)", description = "Soft-deletes a user account and revokes all of their active sessions.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
    }

    @Operation(summary = "Get user roles (Admin)", description = "Retrieves all assigned roles for a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved roles"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Set<UserRole> getUserRoles(@PathVariable UUID id) {
        return userService.getUserRoles(id);
    }

    @Operation(summary = "Assign role (Admin)", description = "Assigns a new role to a user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Role successfully assigned"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: User already has this role")
    })
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addRole(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        userService.addRoleToUser(id, request.role());
    }

    @Operation(summary = "Remove role (Admin)", description = "Removes a specific role from a user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Role successfully removed"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: User does not have this role")
    })
    @DeleteMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRole(@PathVariable UUID id, @PathVariable UserRole role) {
        userService.removeRoleFromUser(id, role);
    }

    @Operation(summary = "Get my active sessions", description = "Retrieves a list of all active device sessions for the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active sessions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/me/sessions")
    @PreAuthorize("isAuthenticated()")
    public List<DeviceSessionResponse> getMySessions(@AuthenticationPrincipal AuthUser authUser) {
        return userService.getUserSessions(authUser.id());
    }

    @Operation(summary = "Revoke my session", description = "Invalidates a specific device session. If the revoked session is the current one, the active JWT is also invalidated.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session successfully revoked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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

    @Operation(summary = "Get user sessions (Admin)", description = "Retrieves all active device sessions for a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active sessions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DeviceSessionResponse> getUserSessionsByAdmin(@PathVariable UUID id) {
        return userService.getUserSessions(id);
    }

    @Operation(summary = "Revoke user session (Admin)", description = "Invalidates a specific device session for a targeted user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session successfully revoked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required")
    })
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

    @Operation(summary = "Request password reset", description = "Initiates the password recovery process by sending an email with a verification code.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password reset initiated (or email ignored if not found to prevent user enumeration)"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Password reset is not available for users registered via external providers"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @RateLimit(maxRequests = 3, timeWindowSeconds = 300)
    @PostMapping("/reset-password/request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {
        userService.initiatePasswordReset(passwordResetRequest.email());
    }

    @Operation(summary = "Confirm password reset", description = "Completes the password recovery process using the code sent via email.")
    @SecurityRequirements()
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password successfully reset"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid code or email mismatch"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @RateLimit(maxRequests = 5, timeWindowSeconds = 60)
    @PostMapping("/reset-password/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest passwordResetConfirmRequest) {
        userService.completePasswordReset(passwordResetConfirmRequest);
    }

    @Operation(summary = "Request email change", description = "Initiates an email change request for a verified user by sending a confirmation code to the new email address.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email change request initiated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Invalid current password)"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Cannot change email for external providers, new email is already taken, or account is unverified"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
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

    @Operation(summary = "Confirm email change", description = "Completes the email change process using the code sent to the new email address.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email successfully changed"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Invalid token)"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Conflict: Email does not match requested change or is already taken"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
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

    @Operation(summary = "Request account deletion", description = "Initiates the account deletion process by sending a confirmation code to the user's email.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deletion request initiated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
    @RateLimit(maxRequests = 3, timeWindowSeconds = 300)
    @PostMapping("/me/delete/request")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestMyAccountDeletion(@AuthenticationPrincipal AuthUser authUser) {
        userService.requestUserDeletion(authUser.id());
    }

    @Operation(summary = "Confirm account deletion", description = "Completes the account deletion process using the verification code. Logs the user out and invalidates sessions.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Invalid token)"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
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
