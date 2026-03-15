package pl.srozga.gluqalc_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.srozga.gluqalc_api.component.rateLimit.RateLimit;
import pl.srozga.gluqalc_api.security.principal.AuthUser;
import pl.srozga.gluqalc_api.service.StatisticsService;

import java.time.LocalDate;

@Tag(name = "08. Statistics", description = "Endpoints for exporting user data and generating analytical reports")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @Operation(summary = "Export data to CSV", description = "Generates and downloads a CSV file containing daily nutritional and insulin statistics for a specified date range.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file successfully generated and ready for download",
                    content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid date format)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT)"),
            @ApiResponse(responseCode = "404", description = "Not found: User profile is missing (required for goal and insulin calculations)"),
            @ApiResponse(responseCode = "429", description = "Too many requests (Rate limit exceeded)")
    })
    @RateLimit(maxRequests = 2, timeWindowSeconds = 60)
    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportData(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        byte[] csvContent = statisticsService.generateCsvExport(user, from, to);

        String filename = String.format("gluqalc_export_%s_%s.csv", from, to);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(csvContent.length)
                .body(csvContent);
    }
}
