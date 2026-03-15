package pl.srozga.gluqalc_api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.dto.request.AddMealCategoryRequest;
import pl.srozga.gluqalc_api.dto.response.MealCategoryResponse;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.MealCategoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/log/categories")
@RequiredArgsConstructor
public class MealCategoryController {
    private final MealCategoryService mealCategoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MealCategoryResponse> getAllCategories(
            @AuthenticationPrincipal AuthUser user
    ) {
        return mealCategoryService.getAllCategories(user);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public MealCategoryResponse createCategory(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody AddMealCategoryRequest request
    ) {
        return mealCategoryService.createCategory(user, request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MealCategoryResponse getCategory(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID id
    ) {
        return mealCategoryService.getCategory(id, user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID id
    ) {
        mealCategoryService.deleteCategory(id, user);
    }
}
