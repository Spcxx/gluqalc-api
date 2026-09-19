package pl.srozga.gluqalc_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.common.MacroType;
import pl.srozga.gluqalc_api.component.nutrition.NutritionCalculator;
import pl.srozga.gluqalc_api.dto.internal.UserCalcDataDto;
import pl.srozga.gluqalc_api.dto.request.UpdateBiometricsRequest;
import pl.srozga.gluqalc_api.dto.request.UpdateUserProfileRequest;
import pl.srozga.gluqalc_api.dto.response.NutritionTargetsResponse;
import pl.srozga.gluqalc_api.dto.response.UserProfileHistoryResponse;
import pl.srozga.gluqalc_api.dto.response.UserProfileResponse;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.entity.UserProfileHistory;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.UserProfileHistoryRepository;
import pl.srozga.gluqalc_api.repository.UserProfileRepository;
import pl.srozga.gluqalc_api.repository.UserRepository;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final NutritionCalculator nutritionCalculator;
    private final UserProfileHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(AuthUser user) {
        return userProfileRepository.findById(user.id())
                .map(this::mapToResponse)
                .orElseThrow(() -> new NotFoundException("User profile not found. Please complete your profile setup."));
    }

    @Transactional
    public UserProfileResponse updateProfile(AuthUser authUser, UpdateUserProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(authUser.id())
                .orElseGet(() -> createNewProfileEntity(authUser.id()));

        boolean biometricsChanged = !Objects.equals(profile.getWeightInKg(), request.weightInKg())
                || !Objects.equals(profile.getHeightInCm(), request.heightInCm())
                || !Objects.equals(profile.getBodyFatPercentage(), request.bodyFatPercentage());

        profile.setGender(request.gender());
        profile.setWeightInKg(request.weightInKg());
        profile.setHeightInCm(request.heightInCm());
        profile.setBirthDate(request.birthDate() != null ? request.birthDate().toString() : null);
        profile.setPhysicalActivityLevel(request.physicalActivityLevel());
        profile.setKcalGoalDifference(request.kcalGoalDifference());
        profile.setBodyFatPercentage(request.bodyFatPercentage());
        profile.setBmrMethod(request.bmrCalculationMethod());
        profile.setInsulinSensitivityFactor(request.insulinSensitivityFactor());
        profile.setInsulinFatProteinRatio(request.insulinFatProteinRatio());
        profile.setInsulinDeliveryMethod(request.insulinDeliveryMethod());
        profile.setCombinedInsulinCalculationMethod(request.combinedInsulinCalculationMethod());
        profile.setTddMultiplier(request.tddMultiplier());
        profile.setDailyBasalInsulin(request.dailyBasalInsulin());

        if (request.hourlyCarbRatio() != null) {
            try {
                profile.setHourlyCarbRatioJson(objectMapper.writeValueAsString(request.hourlyCarbRatio()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing ICR map", e);
            }
        }

        if (request.weeklyKcalDistribution() != null) {
            try {
                profile.setWeeklyKcalDistributionJson(objectMapper.writeValueAsString(request.weeklyKcalDistribution()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing distribution", e);
            }
        }

        if (request.macroStrategy() != null) {
            try {
                profile.setMacroStrategyJson(objectMapper.writeValueAsString(request.macroStrategy()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing macro strategy", e);
            }
        }

        UserProfile savedProfile = userProfileRepository.save(profile);
        if (biometricsChanged && hasAnyBiometrics(request)) {
            saveHistorySnapshot(savedProfile);
            log.info("Biometric changes detected for user: {}. History snapshot saved.", authUser.id());
        }
        log.info("User profile updated for user: {}", authUser.id());

        return mapToResponse(savedProfile);
    }

    @Transactional
    public UserProfileResponse updateBiometrics(AuthUser authUser, UpdateBiometricsRequest request) {
        UserProfile profile = userProfileRepository.findById(authUser.id())
                .orElseThrow(() -> new NotFoundException("User profile not found. Please complete your profile setup first."));

        boolean changed = false;

        BigDecimal previousDailyKcalGoal = getDailyGoalSafely(profile);
        int oldDiff = profile.getKcalGoalDifference() != null ? profile.getKcalGoalDifference() : 0;

        if (request.weightInKg() != null && !Objects.equals(profile.getWeightInKg(), request.weightInKg())) {
            profile.setWeightInKg(request.weightInKg());
            changed = true;
        }
        if (request.heightInCm() != null && !Objects.equals(profile.getHeightInCm(), request.heightInCm())) {
            profile.setHeightInCm(request.heightInCm());
            changed = true;
        }
        if (request.bodyFatPercentage() != null && !Objects.equals(profile.getBodyFatPercentage(), request.bodyFatPercentage())) {
            profile.setBodyFatPercentage(request.bodyFatPercentage());
            changed = true;
        }

        if (changed && previousDailyKcalGoal != null && oldDiff != 0) {
            try {
                BigDecimal newTdee = nutritionCalculator.calculate(profile).tdee();

                int newDiff = previousDailyKcalGoal.subtract(newTdee).intValue();
                if (oldDiff < 0) {
                    newDiff = Math.min(newDiff, 0);
                } else if (oldDiff > 0) {
                    newDiff = Math.max(newDiff, 0);
                }

                profile.setKcalGoalDifference(newDiff);
            } catch (Exception e) {
                log.warn("Could not recalibrate kcal goal difference automatically for user {} due to missing data: {}", profile.getId(), e.getMessage());
            }
        }

        UserProfile savedProfile = userProfileRepository.save(profile);

        if (changed) {
            saveHistorySnapshot(savedProfile);
        }

        log.info("Biometrics partially updated for user: {}", authUser.id());
        return mapToResponse(savedProfile);
    }

    private BigDecimal getDailyGoalSafely(UserProfile profile) {
        try {
            return nutritionCalculator.calculate(profile).dailyGoalKcal();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean hasAnyBiometrics(UpdateUserProfileRequest request) {
        return request.weightInKg() != null || request.heightInCm() != null || request.bodyFatPercentage() != null;
    }

    private void saveHistorySnapshot(UserProfile profile) {
        UserProfileHistory history = UserProfileHistory.builder()
                .user(profile.getUser())
                .weightInKg(profile.getWeightInKg())
                .heightInCm(profile.getHeightInCm())
                .bodyFatPercentage(profile.getBodyFatPercentage())
                .build();
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<UserProfileHistoryResponse> getProfileHistory(AuthUser user) {
        return historyRepository.findAllByUserIdOrderByCreatedAtDesc(user.id())
                .stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    private UserProfileHistoryResponse mapToHistoryResponse(UserProfileHistory entity) {
        BigDecimal bmi = null;
        if (entity.getWeightInKg() != null && entity.getHeightInCm() != null && entity.getHeightInCm().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightInMeters = entity.getHeightInCm().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            bmi = entity.getWeightInKg().divide(heightInMeters.pow(2), 2, RoundingMode.HALF_UP);
        }

        return new UserProfileHistoryResponse(
                entity.getId(),
                entity.getWeightInKg(),
                entity.getHeightInCm(),
                entity.getBodyFatPercentage(),
                bmi,
                entity.getCreatedAt()
        );
    }

    private UserProfile createNewProfileEntity(UUID userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("User account not found"));

        return UserProfile.builder()
                .user(user)
                .build();
    }

    private UserProfileResponse mapToResponse(UserProfile p) {
        Map<DayOfWeek, Integer> distribution = null;
        if (p.getWeeklyKcalDistributionJson() != null) {
            try {
                distribution = objectMapper.readValue(
                        p.getWeeklyKcalDistributionJson(),
                        new TypeReference<>() {}
                );
            } catch (JsonProcessingException e) {
                log.error("Error deserializing distribution for user {}", p.getId());
            }
        }

        Map<Integer, BigDecimal> icrMap = null;
        if (p.getHourlyCarbRatioJson() != null) {
            try {
                icrMap = objectMapper.readValue(
                        p.getHourlyCarbRatioJson(),
                        new TypeReference<>() {}
                );
            } catch (JsonProcessingException e) {
                log.error("Error deserializing ICR for user {}", p.getId());
            }
        }

        Map<MacroType, BigDecimal> macroStrategy = nutritionCalculator.getMacroRatios(p.getMacroStrategyJson());

        NutritionTargetsResponse targets = null;
        try {
            UserCalcDataDto calcResult = nutritionCalculator.calculate(p);
            targets = new NutritionTargetsResponse(
                    calcResult.bmr(),
                    calcResult.tdee(),
                    calcResult.dailyGoalKcal(),
                    calcResult.dailyGoalProtein(),
                    calcResult.dailyGoalFat(),
                    calcResult.dailyGoalCarbs()
            );
        } catch (IllegalArgumentException e) {
            log.debug("Could not calculate nutrition targets for user {}: {}", p.getId(), e.getMessage());
        }

        return new UserProfileResponse(
                p.getGender(),
                p.getWeightInKg(),
                p.getHeightInCm(),
                p.getBirthDate() != null ? LocalDate.parse(p.getBirthDate()) : null,
                p.getAge(),
                p.getPhysicalActivityLevel(),
                p.getKcalGoalDifference(),
                distribution,
                p.getBodyFatPercentage(),
                p.getBmrMethod(),
                macroStrategy,
                p.getInsulinSensitivityFactor(),
                p.getInsulinFatProteinRatio(),
                p.getInsulinDeliveryMethod(),
                p.getCombinedInsulinCalculationMethod(),
                icrMap,
                p.getTddMultiplier(),
                p.getDailyBasalInsulin(),
                targets
        );
    }
}
