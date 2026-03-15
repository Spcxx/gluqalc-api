package pl.srozga.gluqalc_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.srozga.gluqalc_api.dto.request.AddMealEntryRequest;
import pl.srozga.gluqalc_api.dto.response.DaySummaryResponse;
import pl.srozga.gluqalc_api.dto.response.MealCategoryResponse;
import pl.srozga.gluqalc_api.dto.response.MealEntryResponse;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.MealLogService;
import pl.srozga.gluqalc_api.service.MealLogSummaryService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "06. Meal Log", description = "Endpoints for logging meals, tracking daily nutrition and calculating insulin estimates")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/log")
public class MealLogController {
    private final MealLogService mealLogService;
    private final MealLogSummaryService mealLogSummaryService;

    @Operation(summary = "Get daily meal log", description = "Retrieves all logged meal entries for a specific date, grouped by user-defined categories. Defaults to today if no date is provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the daily log"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid date format)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT)")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MealCategoryResponse> getDailyLog(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return mealLogService.getDailyLog(user, targetDate);
    }

    @Operation(summary = "Add a meal entry", description = "Logs a product/food item to the user's daily log. Automatically calculates macronutrients and estimates insulin needs based on the user's health profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meal entry successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., missing required fields)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Meal category, product, or product portion does not exist")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public MealEntryResponse addEntry(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody AddMealEntryRequest request
    ) {
        return mealLogService.addMealEntry(user, request);
    }

    @Operation(summary = "Get a meal entry", description = "Retrieves the details of a specific logged meal entry, including its calculated macronutrients and insulin estimates.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the meal entry"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid UUID format)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Meal entry does not exist or does not belong to the user")
    })
    @GetMapping("/{entryId}")
    @PreAuthorize("isAuthenticated()")
    public MealEntryResponse getEntry(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID entryId
    ) {
        return mealLogService.getMealEntry(entryId, user);
    }

    @Operation(summary = "Delete a meal entry", description = "Permanently removes a specific meal entry from the user's log.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Meal entry successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: Meal entry does not exist or does not belong to the user")
    })
    @DeleteMapping("/{entryId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID entryId
    ) {
        mealLogService.deleteMealEntry(entryId, user);
    }

    @Operation(summary = "Get daily nutritional summary", description = "Calculates total consumed calories, macronutrients and remaining targets for a specific day based on the user's dietary strategy and logged meals.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the daily summary"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid date format)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found: User profile is missing (required for target calculations)")
    })
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public DaySummaryResponse getDailySummary(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return mealLogSummaryService.getDaySummary(user, targetDate);
    }
}
