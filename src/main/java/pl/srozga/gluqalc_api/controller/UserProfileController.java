package pl.srozga.gluqalc_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.dto.request.UpdateUserProfileRequest;
import pl.srozga.gluqalc_api.dto.response.UserProfileResponse;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.UserProfileService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile")
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse getMyProfile(@AuthenticationPrincipal AuthUser user) {
        return userProfileService.getProfile(user);
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse updateMyProfile(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return userProfileService.updateProfile(user, request);
    }
}
