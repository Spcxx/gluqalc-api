package pl.srozga.gluqalc_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.component.diabetes.DiabetesCalculator;
import pl.srozga.gluqalc_api.component.nutrition.NutritionCalculator;
import pl.srozga.gluqalc_api.dto.internal.DiabetesCalcDataDto;
import pl.srozga.gluqalc_api.dto.internal.UserCalcDataDto;
import pl.srozga.gluqalc_api.dto.row.DailyStatsExportRow;
import pl.srozga.gluqalc_api.entity.MealEntry;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.exception.DomainValidationException;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.MealEntryRepository;
import pl.srozga.gluqalc_api.repository.UserProfileRepository;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {
    private final MealEntryRepository mealEntryRepository;
    private final UserProfileRepository userProfileRepository;
    private final DiabetesCalculator diabetesCalculator;
    private final NutritionCalculator nutritionCalculator;

    private static final BigDecimal DEFAULT_KCAL_GOAL = new BigDecimal("2000");
    private static final String CSV_SEPARATOR = ";";
    private static final String CSV_NEWLINE = "\r\n";
    private static final String BOM = "\uFEFF";
    private static final Locale APP_LOCALE = Locale.ENGLISH;
    private static final List<String> CSV_HEADERS = List.of(
            "Date",
            "Day of Week",
            "Meals Count",
            "Eating Window",
            "Kcal Consumed",
            "Kcal Goal",
            "Balance",
            "Protein [g]",
            "Fat [g]",
            "Carbs Total [g]",
            "  - Sugars [g]",
            "  - Fiber [g]",
            "Glycemic Load",
            "Macro Ratio [P/F/C]",
            "Carb Units (WW)",
            "Fat/Prot Units (WBT)",
            "Total Insulin",
            "Insulin (Carbs)",
            "Insulin (F+P)",
            "Avg Insulin/Meal"
    );

    @Transactional(readOnly = true)
    public byte[] generateCsvExport(AuthUser user, LocalDate start, LocalDate end) {
        UserProfile profile = userProfileRepository.findByUserId(user.id())
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        BigDecimal dailyGoal = calculateDailyGoalSafely(profile);

        List<MealEntry> entries = mealEntryRepository.findAllByUserIdAndConsumedAtBetween(user.id(), start, end);
        Map<LocalDate, List<MealEntry>> groupedByDate = entries.stream()
                .collect(Collectors.groupingBy(MealEntry::getConsumedAt));

        StringBuilder csv = new StringBuilder();
        csv.append(BOM);
        csv.append(String.join(CSV_SEPARATOR, CSV_HEADERS)).append(CSV_NEWLINE);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<MealEntry> dailyEntries = groupedByDate.getOrDefault(date, List.of());
            DailyStatsExportRow row = calculateDailyRow(date, dailyEntries, profile, dailyGoal);
            appendRowToCsv(csv, row);
        }

        return csv.toString().getBytes();
    }

    private DailyStatsExportRow calculateDailyRow(LocalDate date, List<MealEntry> entries, UserProfile profile, BigDecimal goal) {
        if (entries.isEmpty())
            return DailyStatsExportRow.empty(date, getDayName(date), goal);

        BigDecimal sumKcal = BigDecimal.ZERO;
        BigDecimal sumCarbs = BigDecimal.ZERO;
        BigDecimal sumSugars = BigDecimal.ZERO;
        BigDecimal sumFiber = BigDecimal.ZERO;
        BigDecimal sumProtein = BigDecimal.ZERO;
        BigDecimal sumFat = BigDecimal.ZERO;
        BigDecimal sumGlycemicLoad = BigDecimal.ZERO;

        LocalTime firstMeal = null;
        LocalTime lastMeal = null;

        for (MealEntry entry : entries) {
            sumKcal = sumKcal.add(defaultZero(entry.getEnergyKcal()));
            sumCarbs = sumCarbs.add(defaultZero(entry.getCarbohydrates()));
            sumProtein = sumProtein.add(defaultZero(entry.getProtein()));
            sumFat = sumFat.add(defaultZero(entry.getFat()));
            sumSugars = sumSugars.add(defaultZero(entry.getSugars()));
            sumFiber = sumFiber.add(defaultZero(entry.getFiber()));

            if (entry.getGlycemicIndex() != null && entry.getCarbohydrates() != null) {
                BigDecimal gl = entry.getCarbohydrates()
                        .multiply(BigDecimal.valueOf(entry.getGlycemicIndex()))
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                sumGlycemicLoad = sumGlycemicLoad.add(gl);
            }

            LocalTime time = entry.getConsumedAtTime();
            if (firstMeal == null || time.isBefore(firstMeal))
                firstMeal = time;
            if (lastMeal == null || time.isAfter(lastMeal))
                lastMeal = time;
        }

        BigDecimal sumInsulinTotal = BigDecimal.ZERO;
        BigDecimal sumInsulinCarb = BigDecimal.ZERO;
        BigDecimal sumInsulinFatProtein = BigDecimal.ZERO;
        BigDecimal sumWW = BigDecimal.ZERO;
        BigDecimal sumWBT = BigDecimal.ZERO;

        Map<UUID, List<MealEntry>> groupedByCategory = entries.stream()
                .collect(Collectors.groupingBy(m -> m.getMealCategory().getId()));

        for (List<MealEntry> categoryMeals : groupedByCategory.values()) {
            if (categoryMeals.isEmpty()) continue;

            BigDecimal catCarbs = BigDecimal.ZERO;
            BigDecimal catProtein = BigDecimal.ZERO;
            BigDecimal catFat = BigDecimal.ZERO;
            BigDecimal catFiber = BigDecimal.ZERO;
            BigDecimal weightedGiSum = BigDecimal.ZERO;
            BigDecimal carbsWithGi = BigDecimal.ZERO;

            for (MealEntry m : categoryMeals) {
                BigDecimal c = defaultZero(m.getCarbohydrates());
                catCarbs = catCarbs.add(c);
                catProtein = catProtein.add(defaultZero(m.getProtein()));
                catFat = catFat.add(defaultZero(m.getFat()));
                catFiber = catFiber.add(defaultZero(m.getFiber()));

                if (m.getGlycemicIndex() != null && c.compareTo(BigDecimal.ZERO) > 0) {
                    weightedGiSum = weightedGiSum.add(c.multiply(BigDecimal.valueOf(m.getGlycemicIndex())));
                    carbsWithGi = carbsWithGi.add(c);
                }
            }

            BigDecimal averageGi = carbsWithGi.compareTo(BigDecimal.ZERO) > 0
                    ? weightedGiSum.divide(carbsWithGi, 0, RoundingMode.HALF_UP)
                    : null;

            LocalTime time = categoryMeals.getFirst().getConsumedAtTime();

            DiabetesCalcDataDto calc = diabetesCalculator.calculate(
                    catCarbs, catProtein, catFat, catFiber, averageGi, profile, time
            );

            if (calc.totalDose() != null) sumInsulinTotal = sumInsulinTotal.add(calc.totalDose().setScale(2, RoundingMode.HALF_UP));
            if (calc.carbDose() != null) sumInsulinCarb = sumInsulinCarb.add(calc.carbDose().setScale(2, RoundingMode.HALF_UP));
            if (calc.fatProteinDose() != null) sumInsulinFatProtein = sumInsulinFatProtein.add(calc.fatProteinDose().setScale(2, RoundingMode.HALF_UP));
            if (calc.carbUnit() != null) sumWW = sumWW.add(calc.carbUnit().setScale(1, RoundingMode.HALF_UP));
            if (calc.fatProteinUnit() != null) sumWBT = sumWBT.add(calc.fatProteinUnit().setScale(1, RoundingMode.HALF_UP));
        }

        BigDecimal balance = sumKcal.subtract(goal);
        BigDecimal avgInsulin = groupedByCategory.isEmpty() ? BigDecimal.ZERO :
                sumInsulinTotal.divide(BigDecimal.valueOf(groupedByCategory.size()), 2, RoundingMode.HALF_UP);

        String macroRatio = calculateMacroRatio(sumKcal, sumProtein, sumFat, sumCarbs);
        String eatingWindow = formatEatingWindow(firstMeal, lastMeal);

        return new DailyStatsExportRow(
                date,
                getDayName(date),
                entries.size(),
                eatingWindow,
                sumKcal,
                goal,
                balance,
                sumProtein,
                sumFat,
                sumCarbs,
                sumSugars,
                sumFiber,
                sumGlycemicLoad,
                macroRatio,
                sumWW,
                sumWBT,
                sumInsulinTotal,
                sumInsulinCarb,
                sumInsulinFatProtein,
                avgInsulin
        );
    }

    private BigDecimal calculateDailyGoalSafely(UserProfile profile) {
        try {
            UserCalcDataDto calcData = nutritionCalculator.calculate(profile);
            return calcData.dailyGoalKcal() != null ? calcData.dailyGoalKcal() : DEFAULT_KCAL_GOAL;
        } catch (IllegalArgumentException | NullPointerException | DomainValidationException e) {
            log.warn("Cannot calculate nutrition goal: {}. Using default.", e.getMessage());
            return DEFAULT_KCAL_GOAL;
        }
    }

    private String formatEatingWindow(LocalTime start, LocalTime end) {
        if (start == null || end == null)
            return "-";

        Duration duration = Duration.between(start, end);
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return String.format("%02d:%02d - %02d:%02d (%dh %02dm)",
                start.getHour(), start.getMinute(),
                end.getHour(), end.getMinute(),
                hours, minutes);
    }

    private void appendRowToCsv(StringBuilder sb, DailyStatsExportRow row) {
        sb.append(row.date()).append(CSV_SEPARATOR)
                .append(row.dayOfWeek()).append(CSV_SEPARATOR)
                .append(row.mealsCount()).append(CSV_SEPARATOR)
                .append(row.eatingWindow()).append(CSV_SEPARATOR)
                .append(formatNum(row.kcalConsumed())).append(CSV_SEPARATOR)
                .append(formatNum(row.kcalGoal())).append(CSV_SEPARATOR)
                .append(formatNum(row.balance())).append(CSV_SEPARATOR)
                .append(formatNum(row.proteinGrams())).append(CSV_SEPARATOR)
                .append(formatNum(row.fatGrams())).append(CSV_SEPARATOR)
                .append(formatNum(row.carbsGrams())).append(CSV_SEPARATOR)
                .append(formatNum(row.sugarsGrams())).append(CSV_SEPARATOR)
                .append(formatNum(row.fiberGrams())).append(CSV_SEPARATOR)
                .append(formatNum(row.glycemicLoad())).append(CSV_SEPARATOR)
                .append(row.macroRatio()).append(CSV_SEPARATOR)
                .append(formatNum(row.cu())).append(CSV_SEPARATOR)
                .append(formatNum(row.fpu())).append(CSV_SEPARATOR)
                .append(formatNum(row.totalInsulin())).append(CSV_SEPARATOR)
                .append(formatNum(row.insulinCarbs())).append(CSV_SEPARATOR)
                .append(formatNum(row.insulinFatProtein())).append(CSV_SEPARATOR)
                .append(formatNum(row.avgInsulinPerMeal()))
                .append(CSV_NEWLINE);
    }

    private String calculateMacroRatio(BigDecimal totalKcal, BigDecimal p, BigDecimal f, BigDecimal c) {
        if (totalKcal.compareTo(BigDecimal.ZERO) == 0) return "0% / 0% / 0%";

        BigDecimal pKcal = p.multiply(new BigDecimal("4"));
        BigDecimal fKcal = f.multiply(new BigDecimal("9"));
        BigDecimal cKcal = c.multiply(new BigDecimal("4"));

        int pPercent = pKcal.multiply(new BigDecimal("100")).divide(totalKcal, 0, RoundingMode.HALF_UP).intValue();
        int fPercent = fKcal.multiply(new BigDecimal("100")).divide(totalKcal, 0, RoundingMode.HALF_UP).intValue();
        int cPercent = cKcal.multiply(new BigDecimal("100")).divide(totalKcal, 0, RoundingMode.HALF_UP).intValue();

        return String.format("%d%% / %d%% / %d%%", pPercent, fPercent, cPercent);
    }

    private String getDayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, APP_LOCALE);
    }

    private String formatNum(BigDecimal num) {
        return num == null ? "0.00" : num.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private BigDecimal defaultZero(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }
}