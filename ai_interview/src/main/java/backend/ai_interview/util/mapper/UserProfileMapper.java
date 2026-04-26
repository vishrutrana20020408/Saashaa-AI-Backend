package backend.ai_interview.util.mapper;

import backend.ai_interview.dto.response.ProfileSummaryResponse;
import backend.ai_interview.dto.response.UserProfileResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.UserProfile;
import backend.ai_interview.util.ProfileFieldNormalizer;
import org.springframework.stereotype.Component;

/**
 * UserProfileMapper
 *
 * Maps UserProfile entity to:
 * - UserProfileResponse
 * - ProfileSummaryResponse
 *
 * Used for:
 * - user profile page
 * - navbar profile summary
 * - resume-to-profile sync flow responses
 */
@Component
@SuppressWarnings("all")
public class UserProfileMapper {

    /**
     * Convert UserProfile entity to full UserProfileResponse DTO.
     */
    public UserProfileResponse toResponse(UserProfile profile) {
        if (profile == null) {
            return UserProfileResponse.fail("User profile not found");
        }

        AppUser user = profile.getUser();

        UserProfileResponse response = UserProfileResponse.ok("User profile fetched successfully");
        response.setUserId(user != null ? user.getUserId() : null);
        response.setFullName(ProfileFieldNormalizer.firstNonBlank(
                profile.getFullName(),
                buildFullName(user)
        ));
        response.setEmail(ProfileFieldNormalizer.firstNonBlank(
                profile.getEmail(),
                user != null ? user.getEmailAddress() : null
        ));
        response.setPhone(ProfileFieldNormalizer.firstNonBlank(
                profile.getPhone(),
                user != null ? user.getMobileNumber() : null
        ));
        response.setHeadline(ProfileFieldNormalizer.firstNonBlank(
                profile.getPreferredHeadline(),
                profile.getHeadline()
        ));
        response.setLocation(ProfileFieldNormalizer.firstNonBlank(
                profile.getPreferredLocation(),
                profile.getLocation()
        ));
        response.setLinkedinUrl(ProfileFieldNormalizer.normalizeLinkedinUrl(profile.getLinkedinUrl()));
        response.setGithubUrl(ProfileFieldNormalizer.normalizeGithubUrl(profile.getGithubUrl()));
        response.setPortfolioUrl(ProfileFieldNormalizer.normalizePortfolioUrl(profile.getPortfolioUrl()));
        response.setProfileSummary(ProfileFieldNormalizer.normalizeSummary(profile.getProfileSummary()));
        response.setCurrentCompany(ProfileFieldNormalizer.normalizeSimpleField(profile.getCurrentCompany()));
        response.setCurrentRole(ProfileFieldNormalizer.normalizeSimpleField(profile.getCurrentRole()));
        response.setHighestEducation(ProfileFieldNormalizer.normalizeSimpleField(profile.getHighestEducation()));
        response.setTopSkillsJson(ProfileFieldNormalizer.normalizeJsonText(profile.getTopSkillsJson()));
        response.setExperienceSummaryJson(ProfileFieldNormalizer.normalizeJsonText(profile.getExperienceSummaryJson()));
        response.setEducationSummaryJson(ProfileFieldNormalizer.normalizeJsonText(profile.getEducationSummaryJson()));
        response.setSourceResumeVersionId(profile.getSourceResumeVersionId());
        response.setProfileSourceType(ProfileFieldNormalizer.normalizeSimpleField(profile.getProfileSourceType()));

        return response;
    }

    /**
     * Convert UserProfile entity to navbar summary DTO.
     */
    public ProfileSummaryResponse toSummaryResponse(UserProfile profile) {
        if (profile == null) {
            return ProfileSummaryResponse.fail("User profile not found");
        }

        AppUser user = profile.getUser();

        ProfileSummaryResponse response = ProfileSummaryResponse.ok("Profile summary fetched successfully");
        response.setUserId(user != null ? user.getUserId() : null);
        response.setFullName(ProfileFieldNormalizer.firstNonBlank(
                profile.getFullName(),
                buildFullName(user)
        ));
        response.setHeadline(ProfileFieldNormalizer.firstNonBlank(
                profile.getPreferredHeadline(),
                profile.getHeadline()
        ));
        response.setEmail(ProfileFieldNormalizer.firstNonBlank(
                profile.getEmail(),
                user != null ? user.getEmailAddress() : null
        ));

        String fullName = ProfileFieldNormalizer.firstNonBlank(
                profile.getFullName(),
                buildFullName(user)
        );
        response.setAvatarInitials(ProfileFieldNormalizer.buildInitials(fullName));

        return response;
    }

    /**
     * Convert UserProfile entity to UserProfileResponse DTO with custom message.
     */
    public UserProfileResponse toResponse(UserProfile profile, String message) {
        UserProfileResponse response = toResponse(profile);
        response.setMessage(message);
        return response;
    }

    /**
     * Convert UserProfile entity to ProfileSummaryResponse DTO with custom message.
     */
    public ProfileSummaryResponse toSummaryResponse(UserProfile profile, String message) {
        ProfileSummaryResponse response = toSummaryResponse(profile);
        response.setMessage(message);
        return response;
    }

    private String buildFullName(AppUser user) {
        if (user == null) {
            return null;
        }

        return ProfileFieldNormalizer.firstNonBlank(
                user.getProfileFullName(),
                join(user.getName(), user.getSurname())
        );
    }

    private String join(String first, String last) {
        String firstValue = first == null ? "" : first.trim();
        String lastValue = last == null ? "" : last.trim();
        String combined = (firstValue + " " + lastValue).trim();
        return combined.isEmpty() ? null : combined;
    }
}