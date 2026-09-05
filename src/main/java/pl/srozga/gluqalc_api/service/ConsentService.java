package pl.srozga.gluqalc_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.srozga.gluqalc_api.dto.request.AcceptConsentsRequest;
import pl.srozga.gluqalc_api.dto.response.ConsentResponse;
import pl.srozga.gluqalc_api.entity.ConsentDefinition;
import pl.srozga.gluqalc_api.entity.User;
import pl.srozga.gluqalc_api.entity.UserConsent;
import pl.srozga.gluqalc_api.exception.ConflictException;
import pl.srozga.gluqalc_api.exception.NotFoundException;
import pl.srozga.gluqalc_api.repository.ConsentDefinitionRepository;
import pl.srozga.gluqalc_api.repository.UserConsentRepository;
import pl.srozga.gluqalc_api.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentService {
    private final ConsentDefinitionRepository definitionRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean hasPendingRequiredConsents(UUID userId) {
        List<ConsentDefinition> requiredConsents = definitionRepository.findAllByActiveTrueAndRequiredTrue();
        if (requiredConsents.isEmpty())
            return false;

        Set<UUID> acceptedDefinitionIds = userConsentRepository.findAcceptedDefinitionIdsByUserId(userId);

        return requiredConsents.stream().anyMatch(req -> !acceptedDefinitionIds.contains(req.getId()));
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> getAllActiveConsents() {
        return definitionRepository.findAllByActiveTrue().stream()
                .map(def -> new ConsentResponse(def.getId(), def.getCode(), def.getContent(), def.getVersion(), def.isRequired()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsentResponse> getPendingConsentsForUser(UUID userId) {
        List<ConsentDefinition> requiredConsents = definitionRepository.findAllByActiveTrueAndRequiredTrue();
        Set<UUID> acceptedIds = userConsentRepository.findAllByUserId(userId).stream()
                .map(uc -> uc.getConsentDefinition().getId())
                .collect(Collectors.toSet());

        return requiredConsents.stream()
                .filter(def -> !acceptedIds.contains(def.getId()))
                .map(def -> new ConsentResponse(def.getId(), def.getCode(), def.getContent(), def.getVersion(), def.isRequired()))
                .toList();
    }

    @Transactional
    public void acceptConsents(UUID userId, AcceptConsentsRequest request, String ipAddress) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<ConsentDefinition> definitionsToAccept = definitionRepository.findAllByIdInAndActiveTrue(request.consentDefinitionIds());

        if (definitionsToAccept.size() != request.consentDefinitionIds().size())
            throw new ConflictException("Some of the provided consents are invalid or inactive");

        Set<UUID> alreadyAcceptedIds = userConsentRepository.findAllByUserId(userId).stream()
                .map(uc -> uc.getConsentDefinition().getId())
                .collect(Collectors.toSet());

        List<UserConsent> newConsents = definitionsToAccept.stream()
                .filter(def -> !alreadyAcceptedIds.contains(def.getId()))
                .map(def -> UserConsent.builder()
                        .user(user)
                        .consentDefinition(def)
                        .ipAddress(ipAddress)
                        .build())
                .toList();

        userConsentRepository.saveAll(newConsents);
        log.info("User {} accepted {} new consents from IP {}", userId, newConsents.size(), ipAddress);
    }

    @Transactional(readOnly = true)
    public void validateAllActiveConsentsAccepted(List<UUID> consentDefinitionIds) {
        if (consentDefinitionIds == null)
            throw new ConflictException("No consents provided for validation");

        if (consentDefinitionIds.stream().anyMatch(Objects::isNull))
            throw new ConflictException("Consent ID cannot be null");

        Set<UUID> uniqueIds = new HashSet<>(consentDefinitionIds);
        if (uniqueIds.size() != consentDefinitionIds.size())
            throw new ConflictException("Duplicate consent IDs are not allowed");

        List<ConsentDefinition> activeConsents = definitionRepository.findAllByActiveTrue();
        if (activeConsents.isEmpty())
            return;

        Set<UUID> activeIds = activeConsents.stream().map(ConsentDefinition::getId).collect(Collectors.toSet());
        if (!activeIds.containsAll(uniqueIds))
            throw new ConflictException("Some of the provided consents are invalid or inactive");
        if (!uniqueIds.containsAll(activeIds))
            throw new ConflictException("You must provide all active consents");
    }
}
