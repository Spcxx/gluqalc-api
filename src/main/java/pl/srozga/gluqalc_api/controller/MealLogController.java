package pl.srozga.gluqalc_api.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/log")
public class MealLogController {
    private final MealLogService mealLogService;
    private final MealLogSummaryService mealLogSummaryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MealCategoryResponse> getDailyLog(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return mealLogService.getDailyLog(user, targetDate);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public MealEntryResponse addEntry(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody AddMealEntryRequest request
    ) {
        return mealLogService.addMealEntry(user, request);
    }

    @GetMapping("/{entryId}")
    @PreAuthorize("isAuthenticated()")
    public MealEntryResponse getEntry(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID entryId
    ) {
        return mealLogService.getMealEntry(entryId, user);
    }

    @DeleteMapping("/{entryId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable UUID entryId
    ) {
        mealLogService.deleteMealEntry(entryId, user);
    }

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
