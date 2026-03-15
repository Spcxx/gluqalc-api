package pl.srozga.gluqalc_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.dto.request.UpdateUserProfileRequest;
import pl.srozga.gluqalc_api.dto.response.UserProfileResponse;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.UserProfileService;

@Tag(name = "04. User Profile", description = "Endpoints for managing personal health settings, diabetic parameters and nutritional goals")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class UserProfileController {
    private final UserProfileService userProfileService;

    @Operation(summary = "Get my profile", description = "Retrieves the authenticated user's health profile, including calculated nutritional targets and insulin calculation settings. Sensitive data is decrypted on the fly.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT)"),
            @ApiResponse(responseCode = "404", description = "Not found: The user has not set up their profile yet")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse getMyProfile(@AuthenticationPrincipal AuthUser user) {
        return userProfileService.getProfile(user);
    }

    @Operation(summary = "Update my profile", description = "Updates or creates the user's health profile. This data is critical and directly impacts all insulin and dietary calculations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile successfully updated (or created)"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid JSON maps or negative body metrics)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Associated user account does not exist")
    })
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse updateMyProfile(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userProfileService.updateProfile(user, request);
    }
}
