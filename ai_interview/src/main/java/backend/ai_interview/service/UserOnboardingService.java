package backend.ai_interview.service;

import backend.ai_interview.dto.request.UserOnboardingRequest;
import backend.ai_interview.dto.response.UserOnboardingResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.UserProfile;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.UserProfileRepository;
import backend.ai_interview.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserOnboardingService
 *
 * Saves and loads onboarding selections from DB (AppUser columns).
 *
 * Stores multi selections as JSON strings in AppUser:
 * - onboardingSubDomainMulti  (JSON array)
 * - onboardingJobTitles       (JSON array)
 *
 * Latest project update:
 * - ensures official user profile record exists for navbar/profile module
 * - marks account/profile initialization consistently during onboarding flow
 * - stays aligned with updated onboarding request/response and profile continuity flow
 */
@Service
@SuppressWarnings("all")
public class UserOnboardingService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;

    public UserOnboardingService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Save onboarding selections
     */
    @Transactional
    public UserOnboardingResponse save(String userId, UserOnboardingRequest request) {

        validateUserId(userId);

        if (request == null) {
            throw new ApiException("Invalid onboarding request.");
        }

        String domain = normalizeDomain(request.getDomain());
        String mode = normalizeMode(request.getSubDomainMode());

        if (isBlank(domain)) {
            throw new ApiException("Domain is required.");
        }
        if (isBlank(mode)) {
            throw new ApiException("Sub-domain mode is required.");
        }

        if (!domain.equals("Technical") && !domain.equals("Non-Technical")) {
            throw new ApiException("Invalid domain. Allowed: Technical, Non-Technical.");
        }

        if (!mode.equals("single") && !mode.equals("multi") && !mode.equals("any")) {
            throw new ApiException("Invalid sub-domain mode. Allowed: single, multi, any.");
        }

        String single = safe(request.getSubDomainSingle());
        List<String> multi = normalizeStringList(request.getSubDomainMulti());
        List<String> jobTitles = normalizeStringList(request.getJobTitles());

        if (mode.equals("single")) {
            if (single.isBlank()) {
                throw new ApiException("Sub-domain is required for mode=single.");
            }
            multi = Collections.emptyList();
        }

        if (mode.equals("multi")) {
            if (multi.isEmpty()) {
                throw new ApiException("At least one sub-domain is required for mode=multi.");
            }
            single = "";
            jobTitles = Collections.emptyList();
        }

        if (mode.equals("any")) {
            single = "";
            multi = Collections.emptyList();
            jobTitles = Collections.emptyList();
        }

        AppUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        user.setOnboardingDomain(domain);
        user.setOnboardingSubDomainMode(mode);
        user.setOnboardingSubDomainSingle(mode.equals("single") ? single : "");
        user.setOnboardingSubDomainMulti(toJson(multi));
        user.setOnboardingJobTitles(toJson(jobTitles));
        user.setClass10MarksheetUrl(request.getClass10MarksheetUrl());
        user.setClass12MarksheetUrl(request.getClass12MarksheetUrl());
        user.setGraduationMarksheetUrl(request.getGraduationMarksheetUrl());
        user.setPostGraduationMarksheetUrl(request.getPostGraduationMarksheetUrl());
        user.setOnboardingDone(true);

        initializeUserProfileIfMissing(user);

        userRepository.save(user);

        return UserOnboardingResponse.builder()
                .success(true)
                .message("Onboarding saved successfully")
                .done(true)
                .domain(domain)
                .subDomainMode(mode)
                .subDomainSingle(mode.equals("single") ? single : "")
                .subDomainMulti(mode.equals("multi") ? multi : Collections.emptyList())
                .jobTitles(jobTitles)
                .class10MarksheetUrl(user.getClass10MarksheetUrl())
                .class12MarksheetUrl(user.getClass12MarksheetUrl())
                .graduationMarksheetUrl(user.getGraduationMarksheetUrl())
                .postGraduationMarksheetUrl(user.getPostGraduationMarksheetUrl())
                .build();
    }

    /**
     * Load onboarding selections
     */
    @Transactional(readOnly = true)
    public UserOnboardingResponse get(String userId) {

        validateUserId(userId);

        AppUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        if (!user.isOnboardingDone()) {
            return UserOnboardingResponse.builder()
                    .success(true)
                    .message("Onboarding not completed")
                    .done(false)
                    .domain("")
                    .subDomainMode("")
                    .subDomainSingle("")
                    .subDomainMulti(Collections.emptyList())
                    .jobTitles(Collections.emptyList())
                    .build();
        }

        String domain = safe(user.getOnboardingDomain());
        String mode = normalizeMode(user.getOnboardingSubDomainMode());
        String single = safe(user.getOnboardingSubDomainSingle());

        List<String> multi = fromJsonList(user.getOnboardingSubDomainMulti());
        List<String> jobTitles = fromJsonList(user.getOnboardingJobTitles());

        return UserOnboardingResponse.builder()
                .success(true)
                .message("Onboarding loaded successfully")
                .done(true)
                .domain(domain)
                .subDomainMode(mode)
                .subDomainSingle(single)
                .subDomainMulti(multi)
                .jobTitles(jobTitles)
                .class10MarksheetUrl(user.getClass10MarksheetUrl())
                .class12MarksheetUrl(user.getClass12MarksheetUrl())
                .graduationMarksheetUrl(user.getGraduationMarksheetUrl())
                .postGraduationMarksheetUrl(user.getPostGraduationMarksheetUrl())
                .build();
    }

    /**
     * Reset onboarding selections
     */
    @Transactional
    public void reset(String userId) {
        validateUserId(userId);

        AppUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        user.setOnboardingDomain("");
        user.setOnboardingSubDomainMode("");
        user.setOnboardingSubDomainSingle("");
        user.setOnboardingSubDomainMulti(toJson(Collections.emptyList()));
        user.setOnboardingJobTitles(toJson(Collections.emptyList()));
        user.setOnboardingDone(false);

        userRepository.save(user);
    }

    /**
     * Latest project update:
     * Ensure the official user profile record exists so that
     * navbar/profile page can work immediately after onboarding.
     */
    private void initializeUserProfileIfMissing(AppUser user) {
        if (user == null || isBlank(user.getUserId())) {
            return;
        }

        boolean exists = userProfileRepository.existsByUser_UserId(user.getUserId());

        if (!exists) {
            String first = safe(user.getName());
            String last = safe(user.getSurname());
            String fullName = (first + " " + last).trim();

            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .fullName(fullName.isBlank() ? null : fullName)
                    .email(normalizeEmail(user.getEmailAddress()))
                    .phone(trimToNull(user.getMobileNumber()))
                    .profileSourceType("MANUAL")
                    .build();

            userProfileRepository.save(profile);
        }

        user.setProfileCreated(true);

        if (isBlank(user.getProfileFullName())) {
            String first = safe(user.getName());
            String last = safe(user.getSurname());
            String fullName = (first + " " + last).trim();
            user.setProfileFullName(fullName.isBlank() ? null : fullName);
        }

        if (isBlank(user.getProfileSourceType())) {
            user.setProfileSourceType("MANUAL");
        }
    }

    // ========================
    // JSON Helpers
    // ========================

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list == null ? Collections.emptyList() : list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJsonList(String json) {
        try {
            if (json == null || json.isBlank()) {
                return Collections.emptyList();
            }
            return normalizeStringList(objectMapper.readValue(json, new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<String> normalizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        return values.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeDomain(String domain) {
        String value = safe(domain);
        if (value.equalsIgnoreCase("technical")) {
            return "Technical";
        }
        if (value.equalsIgnoreCase("non-technical") || value.equalsIgnoreCase("non technical")) {
            return "Non-Technical";
        }
        return value;
    }

    private String normalizeMode(String mode) {
        String value = safe(mode).toLowerCase();

        if (value.equals("multiple")) {
            return "multi";
        }

        return value;
    }

    private String normalizeEmail(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ApiException("Invalid user session. Please login again.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}