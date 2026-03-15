package pl.srozga.gluqalc_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "05. Meal Categories", description = "Endpoints for managing user-specific meal categories")
@RestController
@RequestMapping("/api/v1/log/categories")
@RequiredArgsConstructor
public class MealCategoryController {
    private final MealCategoryService mealCategoryService;

    @Operation(summary = "Get all categories", description = "Retrieves a list of all meal categories defined by the authenticated user, ordered by their sort order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved meal categories"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT)")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MealCategoryResponse> getAllCategories(
            @AuthenticationPrincipal AuthUser user
    ) {
        return mealCategoryService.getAllCategories(user);
    }

    @Operation(summary = "Create a new category", description = "Creates a new meal category for the authenticated user. The sort order is automatically assigned to the end of the list.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., missing name)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public MealCategoryResponse createCategory(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody AddMealCategoryRequest request
    ) {
        return mealCategoryService.createCategory(user, request);
    }

    @Operation(summary = "Get a specific category", description = "Retrieves the details of a specific meal category by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the category"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid UUID format)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Category does not exist or does not belong to the user")
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MealCategoryResponse getCategory(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID id
    ) {
        return mealCategoryService.getCategory(id, user);
    }

    @Operation(summary = "Delete a category", description = "Deletes a specific meal category. This operation will fail if there are any meal entries associated with this category.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid UUID format)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Category does not exist or does not belong to the user"),
            @ApiResponse(responseCode = "409", description = "Conflict: Cannot delete a category that contains existing meal entries")
    })
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
