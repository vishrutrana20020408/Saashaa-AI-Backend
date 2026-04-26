package backend.ai_interview.service.profile;

import backend.ai_interview.dto.request.ProfilePreferenceUpdateRequest;
import backend.ai_interview.dto.request.UserProfileUpdateRequest;
import backend.ai_interview.dto.response.ProfileSummaryResponse;
import backend.ai_interview.dto.response.UserProfileResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.UserProfile;
import backend.ai_interview.entity.enums.ProfileSourceType;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.exception.ProfileNotFoundException;
import backend.ai_interview.repository.UserProfileRepository;
import backend.ai_interview.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserProfileService
 *
 * Handles:
 * - loading logged-in user's official profile
 * - creating default profile if missing
 * - updating user profile manually
 * - loading navbar summary
 * - updating profile preferences
 *
 * Latest project update:
 * - supports navbar-based profile module
 * - supports database-backed user profile storage
 * - keeps AppUser and UserProfile data aligned
 * - stays aligned with resume/profile sync and resume-version based flows
 */
@Service
@SuppressWarnings("all")
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public UserProfileService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Fetch logged-in user's full profile.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String userId) {
        validateUserId(userId);

        UserProfile profile = getOrCreateProfile(userId);
        return toResponse(profile, "User profile fetched successfully");
    }

    /**
     * Update logged-in user's profile.
     */
    @Transactional
    public UserProfileResponse updateMyProfile(String userId, UserProfileUpdateRequest request) {
        validateUserId(userId);

        if (request == null) {
            throw new ApiException("Invalid profile update request.");
        }

        UserProfile profile = getOrCreateProfile(userId);

        if (request.getFullName() != null) {
            profile.setFullName(trimToNull(request.getFullName()));
        }
        if (request.getEmail() != null) {
            profile.setEmail(normalizeEmail(request.getEmail()));
        }
        if (request.getPhone() != null) {
            profile.setPhone(trimToNull(request.getPhone()));
        }
        if (request.getHeadline() != null) {
            profile.setHeadline(trimToNull(request.getHeadline()));
        }
        if (request.getLocation() != null) {
            profile.setLocation(trimToNull(request.getLocation()));
        }
        if (request.getLinkedinUrl() != null) {
            profile.setLinkedinUrl(trimToNull(request.getLinkedinUrl()));
        }
        if (request.getGithubUrl() != null) {
            profile.setGithubUrl(trimToNull(request.getGithubUrl()));
        }
        if (request.getPortfolioUrl() != null) {
            profile.setPortfolioUrl(trimToNull(request.getPortfolioUrl()));
        }
        if (request.getProfileSummary() != null) {
            profile.setProfileSummary(trimToNull(request.getProfileSummary()));
        }
        if (request.getCurrentCompany() != null) {
            profile.setCurrentCompany(trimToNull(request.getCurrentCompany()));
        }
        if (request.getCurrentRole() != null) {
            profile.setCurrentRole(trimToNull(request.getCurrentRole()));
        }
        if (request.getHighestEducation() != null) {
            profile.setHighestEducation(trimToNull(request.getHighestEducation()));
        }
        if (request.getTopSkillsJson() != null) {
            profile.setTopSkillsJson(trimToNull(request.getTopSkillsJson()));
        }
        if (request.getExperienceSummaryJson() != null) {
            profile.setExperienceSummaryJson(trimToNull(request.getExperienceSummaryJson()));
        }
        if (request.getEducationSummaryJson() != null) {
            profile.setEducationSummaryJson(trimToNull(request.getEducationSummaryJson()));
        }

        if (request.getClass10MarksheetUrl() != null) {
            profile.setClass10MarksheetUrl(trimToNull(request.getClass10MarksheetUrl()));
        }
        if (request.getClass12MarksheetUrl() != null) {
            profile.setClass12MarksheetUrl(trimToNull(request.getClass12MarksheetUrl()));
        }
        if (request.getGraduationMarksheetUrl() != null) {
            profile.setGraduationMarksheetUrl(trimToNull(request.getGraduationMarksheetUrl()));
        }
        if (request.getPostGraduationMarksheetUrl() != null) {
            profile.setPostGraduationMarksheetUrl(trimToNull(request.getPostGraduationMarksheetUrl()));
        }
        if (request.getExperienceYears() != null) {
            profile.setExperienceYears(request.getExperienceYears());
        }
        if (request.getVerified() != null) {
            profile.setVerified(request.getVerified());
        }
        if (request.getProfilePictureUrl() != null) {
            profile.setProfilePictureUrl(trimToNull(request.getProfilePictureUrl()));
        }

        profile.setProfileSourceType(resolveUpdatedSourceType(profile.getProfileSourceType()));

        profile = userProfileRepository.save(profile);
        syncAppUserFromProfile(profile);

        return toResponse(profile, "User profile updated successfully");
    }

    /**
     * Fetch compact navbar summary.
     */
    @Transactional(readOnly = true)
    public ProfileSummaryResponse getNavbarSummary(String userId) {
        validateUserId(userId);

        UserProfile profile = getOrCreateProfile(userId);

        ProfileSummaryResponse response = ProfileSummaryResponse.ok("Profile summary fetched successfully");
        response.setUserId(profile.getUser() != null ? profile.getUser().getUserId() : null);
        response.setFullName(firstNonBlank(
                profile.getFullName(),
                buildFullName(profile.getUser())
        ));
        response.setHeadline(firstNonBlank(profile.getPreferredHeadline(), profile.getHeadline(), ""));
        response.setEmail(firstNonBlank(
                profile.getEmail(),
                profile.getUser() != null ? profile.getUser().getEmailAddress() : null,
                ""
        ));

        return response;
    }

    /**
     * Optional helper for profile preferences.
     */
    @Transactional
    public UserProfileResponse updatePreferences(String userId, ProfilePreferenceUpdateRequest request) {
        validateUserId(userId);

        if (request == null) {
            throw new ApiException("Invalid profile preference update request.");
        }

        UserProfile profile = getOrCreateProfile(userId);

        if (request.getAutoSyncFromResume() != null) {
            profile.setAutoSyncFromResume(request.getAutoSyncFromResume());
        }
        if (request.getAllowResumeOverwrite() != null) {
            profile.setAllowResumeOverwrite(request.getAllowResumeOverwrite());
        }
        if (request.getProfileVisibleToAdmin() != null) {
            profile.setProfileVisibleToAdmin(request.getProfileVisibleToAdmin());
        }
        if (request.getProfileVisibleInDashboard() != null) {
            profile.setProfileVisibleInDashboard(request.getProfileVisibleInDashboard());
        }
        if (request.getPreferredHeadline() != null) {
            profile.setPreferredHeadline(trimToNull(request.getPreferredHeadline()));
        }
        if (request.getPreferredLocation() != null) {
            profile.setPreferredLocation(trimToNull(request.getPreferredLocation()));
        }

        profile = userProfileRepository.save(profile);
        return toResponse(profile, "Profile preferences updated successfully");
    }

    /**
     * Get existing profile or create a default one from AppUser.
     */
    @Transactional
    public UserProfile getOrCreateProfile(String userId) {
        validateUserId(userId);

        return userProfileRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    AppUser user = userRepository.findByUserId(userId)
                            .orElseThrow(() -> new ProfileNotFoundException("User not found: " + userId));

                    UserProfile profile = UserProfile.builder()
                            .user(user)
                            .fullName(firstNonBlank(user.getProfileFullName(), buildFullName(user)))
                            .email(firstNonBlank(normalizeEmail(user.getEmailAddress()), null))
                            .phone(firstNonBlank(trimToNull(user.getMobileNumber()), null))
                            .headline(trimToNull(user.getProfileHeadline()))
                            .location(trimToNull(user.getProfileLocation()))
                            .profileSummary(trimToNull(user.getProfileSummary()))
                            .linkedinUrl(trimToNull(user.getLinkedinUrl()))
                            .githubUrl(trimToNull(user.getGithubUrl()))
                            .portfolioUrl(trimToNull(user.getPortfolioUrl()))
                            .currentCompany(trimToNull(user.getCurrentCompany()))
                            .currentRole(trimToNull(user.getCurrentRole()))
                            .highestEducation(trimToNull(user.getHighestEducation()))
                            .topSkillsJson(trimToNull(user.getTopSkillsJson()))
                            .experienceSummaryJson(trimToNull(user.getExperienceSummaryJson()))
                            .educationSummaryJson(trimToNull(user.getEducationSummaryJson()))
                            .profileSourceType(firstNonBlank(
                                    normalizeProfileSourceType(user.getProfileSourceType()),
                                    ProfileSourceType.MANUAL.name()
                            ))
                            .sourceResumeVersionId(user.getSourceResumeVersionId())
                            .build();

                    UserProfile saved = userProfileRepository.save(profile);

                    user.setProfileCreated(true);
                    if (isBlank(user.getProfileFullName())) {
                        user.setProfileFullName(saved.getFullName());
                    }
                    if (isBlank(user.getProfileSourceType())) {
                        user.setProfileSourceType(saved.getProfileSourceType());
                    }
                    userRepository.save(user);

                    return saved;
                });
    }

    /**
     * Convert entity to response.
     */
    private UserProfileResponse toResponse(UserProfile profile, String message) {
        if (profile == null) {
            return UserProfileResponse.fail("User profile not found.");
        }

        UserProfileResponse response = UserProfileResponse.ok(message);
        response.setUserId(profile.getUser() != null ? profile.getUser().getUserId() : null);
        response.setFullName(profile.getFullName());
        response.setEmail(profile.getEmail());
        response.setPhone(profile.getPhone());
        response.setHeadline(firstNonBlank(profile.getPreferredHeadline(), profile.getHeadline(), null));
        response.setLocation(firstNonBlank(profile.getPreferredLocation(), profile.getLocation(), null));
        response.setLinkedinUrl(profile.getLinkedinUrl());
        response.setGithubUrl(profile.getGithubUrl());
        response.setPortfolioUrl(profile.getPortfolioUrl());
        response.setProfileSummary(profile.getProfileSummary());
        response.setCurrentCompany(profile.getCurrentCompany());
        response.setCurrentRole(profile.getCurrentRole());
        response.setHighestEducation(profile.getHighestEducation());
        response.setTopSkillsJson(profile.getTopSkillsJson());
        response.setExperienceSummaryJson(profile.getExperienceSummaryJson());
        response.setEducationSummaryJson(profile.getEducationSummaryJson());
        response.setSourceResumeVersionId(profile.getSourceResumeVersionId());
        response.setProfileSourceType(profile.getProfileSourceType());

        response.setClass10MarksheetUrl(profile.getClass10MarksheetUrl());
        response.setClass12MarksheetUrl(profile.getClass12MarksheetUrl());
        response.setGraduationMarksheetUrl(profile.getGraduationMarksheetUrl());
        response.setPostGraduationMarksheetUrl(profile.getPostGraduationMarksheetUrl());
        response.setExperienceYears(profile.getExperienceYears());
        response.setVerified(profile.isVerified());
        response.setProfilePictureUrl(profile.getProfilePictureUrl());

        return response;
    }

    /**
     * Keep account-level AppUser profile fields aligned with official UserProfile.
     */
    private void syncAppUserFromProfile(UserProfile profile) {
        if (profile == null || profile.getUser() == null) {
            return;
        }

        AppUser user = profile.getUser();
        user.setProfileCreated(true);
        user.setProfileFullName(trimToNull(profile.getFullName()));
        user.setProfileHeadline(trimToNull(profile.getHeadline()));
        user.setProfileLocation(trimToNull(profile.getLocation()));
        user.setProfileSummary(trimToNull(profile.getProfileSummary()));
        user.setLinkedinUrl(trimToNull(profile.getLinkedinUrl()));
        user.setGithubUrl(trimToNull(profile.getGithubUrl()));
        user.setPortfolioUrl(trimToNull(profile.getPortfolioUrl()));
        user.setCurrentCompany(trimToNull(profile.getCurrentCompany()));
        user.setCurrentRole(trimToNull(profile.getCurrentRole()));
        user.setHighestEducation(trimToNull(profile.getHighestEducation()));
        user.setTopSkillsJson(trimToNull(profile.getTopSkillsJson()));
        user.setExperienceSummaryJson(trimToNull(profile.getExperienceSummaryJson()));
        user.setEducationSummaryJson(trimToNull(profile.getEducationSummaryJson()));
        user.setProfileSourceType(normalizeProfileSourceType(profile.getProfileSourceType()));
        user.setSourceResumeVersionId(profile.getSourceResumeVersionId());

        user.setClass10MarksheetUrl(profile.getClass10MarksheetUrl());
        user.setClass12MarksheetUrl(profile.getClass12MarksheetUrl());
        user.setGraduationMarksheetUrl(profile.getGraduationMarksheetUrl());
        user.setPostGraduationMarksheetUrl(profile.getPostGraduationMarksheetUrl());
        user.setExperienceYears(profile.getExperienceYears());
        user.setVerified(profile.isVerified());
        user.setProfilePictureUrl(profile.getProfilePictureUrl());

        checkVerificationStatus(profile);
        user.setVerified(profile.isVerified());

        userRepository.save(user);
    }

    private void checkVerificationStatus(UserProfile profile) {
        boolean docsUploaded = profile.getClass10MarksheetUrl() != null &&
                               profile.getClass12MarksheetUrl() != null &&
                               profile.getFullName() != null &&
                               profile.getEmail() != null;
        
        // Graduation and Post Graduation are optional depending on job type, 
        // but for general verification we check the core ones.
        profile.setVerified(docsUploaded);
    }

    private void validateUserId(String userId) {
        if (isBlank(userId)) {
            throw new ApiException("Invalid user session. Please login again.");
        }
    }

    private String buildFullName(AppUser user) {
        if (user == null) {
            return null;
        }
        String first = trimToEmpty(user.getName());
        String last = trimToEmpty(user.getSurname());
        String full = (first + " " + last).trim();
        return full.isBlank() ? null : full;
    }

    private String resolveUpdatedSourceType(String existingSourceType) {
        if (isBlank(existingSourceType)) {
            return ProfileSourceType.MANUAL.name();
        }

        String normalized = existingSourceType.trim().toUpperCase();
        if (ProfileSourceType.RESUME.name().equals(normalized)) {
            return ProfileSourceType.MIXED.name();
        }

        if (ProfileSourceType.MANUAL.name().equals(normalized)) {
            return ProfileSourceType.MANUAL.name();
        }

        return ProfileSourceType.MIXED.name();
    }

    private String normalizeEmail(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private String normalizeProfileSourceType(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim().toUpperCase();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}