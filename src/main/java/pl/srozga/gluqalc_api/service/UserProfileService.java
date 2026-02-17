package pl.srozga.gluqalc_api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.dto.request.UpdateUserProfileRequest;
import pl.srozga.gluqalc_api.dto.response.UserProfileResponse;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.entity.UserProfile;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.UserProfileRepository;
import pl.srozga.gluqalc_api.repository.UserRepository;
import pl.srozga.gluqalc_api.security.principal.AuthUser;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

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

        profile.setGender(request.gender());
        profile.setWeightInKg(request.weightInKg());
        profile.setHeightInCm(request.heightInCm());
        profile.setBirthDate(request.birthDate() != null ? request.birthDate().toString() : null);
        profile.setPhysicalActivityLevel(request.physicalActivityLevel());
        profile.setKcalGoalDifference(request.kcalGoalDifference());

        if (request.weeklyKcalDistribution() != null) {
            try {
                profile.setWeeklyKcalDistributionJson(objectMapper.writeValueAsString(request.weeklyKcalDistribution()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing distribution", e);
            }
        }

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("User profile updated for user: {}", authUser.id());

        return mapToResponse(savedProfile);
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

        return new UserProfileResponse(
                p.getGender(),
                p.getWeightInKg(),
                p.getHeightInCm(),
                p.getBirthDate() != null ? LocalDate.parse(p.getBirthDate()) : null,
                calculateAge(p.getBirthDate() != null ? LocalDate.parse(p.getBirthDate()) : null),
                p.getPhysicalActivityLevel(),
                p.getKcalGoalDifference(),
                distribution
        );
    }

    private Integer calculateAge(LocalDate birthDate) {
        if (birthDate == null)
            return null;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
