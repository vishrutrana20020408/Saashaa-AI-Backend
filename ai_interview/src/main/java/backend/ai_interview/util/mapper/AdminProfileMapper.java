package backend.ai_interview.util.mapper;

import backend.ai_interview.dto.response.AdminProfileResponse;
import backend.ai_interview.dto.response.ProfileSummaryResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AdminProfile;
import backend.ai_interview.util.ProfileFieldNormalizer;
import org.springframework.stereotype.Component;

/**
 * AdminProfileMapper
 *
 * Maps AdminProfile entity to:
 * - AdminProfileResponse
 * - ProfileSummaryResponse
 *
 * Used for:
 * - admin profile page
 * - navbar profile summary
 * - resume-to-profile sync flow responses
 */
@Component
@SuppressWarnings("all")
public class AdminProfileMapper {

    /**
     * Convert AdminProfile entity to full AdminProfileResponse DTO.
     */
    public AdminProfileResponse toResponse(AdminProfile profile) {
        if (profile == null) {
            return AdminProfileResponse.fail("Admin profile not found");
        }

        Admin admin = profile.getAdmin();

        AdminProfileResponse response = AdminProfileResponse.ok("Admin profile fetched successfully");
        response.setAdminId(admin != null ? admin.getAdminId() : null);
        response.setFullName(ProfileFieldNormalizer.firstNonBlank(
                profile.getFullName(),
                buildFullName(admin)
        ));
        response.setEmail(ProfileFieldNormalizer.firstNonBlank(
                profile.getEmail(),
                readAdminEmail(admin)
        ));
        response.setPhone(ProfileFieldNormalizer.firstNonBlank(
                profile.getPhone(),
                readAdminPhone(admin)
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
        response.setTopSkillsJson(ProfileFieldNormalizer.normalizeJsonText(profile.getTopSkillsJson()));
        response.setSourceResumeVersionId(profile.getSourceResumeVersionId());
        response.setProfileSourceType(ProfileFieldNormalizer.normalizeSimpleField(profile.getProfileSourceType()));

        return response;
    }

    /**
     * Convert AdminProfile entity to navbar summary DTO.
     */
    public ProfileSummaryResponse toSummaryResponse(AdminProfile profile) {
        if (profile == null) {
            return ProfileSummaryResponse.fail("Admin profile not found");
        }

        Admin admin = profile.getAdmin();

        ProfileSummaryResponse response = ProfileSummaryResponse.ok("Profile summary fetched successfully");
        response.setAdminId(admin != null ? admin.getAdminId() : null);
        response.setFullName(ProfileFieldNormalizer.firstNonBlank(
                profile.getFullName(),
                buildFullName(admin)
        ));
        response.setHeadline(ProfileFieldNormalizer.firstNonBlank(
                profile.getPreferredHeadline(),
                profile.getHeadline()
        ));
        response.setEmail(ProfileFieldNormalizer.firstNonBlank(
                profile.getEmail(),
                readAdminEmail(admin)
        ));

        String fullName = ProfileFieldNormalizer.firstNonBlank(
                profile.getFullName(),
                buildFullName(admin)
        );
        response.setAvatarInitials(ProfileFieldNormalizer.buildInitials(fullName));

        return response;
    }

    /**
     * Convert AdminProfile entity to AdminProfileResponse DTO with custom message.
     */
    public AdminProfileResponse toResponse(AdminProfile profile, String message) {
        AdminProfileResponse response = toResponse(profile);
        response.setMessage(message);
        return response;
    }

    /**
     * Convert AdminProfile entity to ProfileSummaryResponse DTO with custom message.
     */
    public ProfileSummaryResponse toSummaryResponse(AdminProfile profile, String message) {
        ProfileSummaryResponse response = toSummaryResponse(profile);
        response.setMessage(message);
        return response;
    }

    private String buildFullName(Admin admin) {
        if (admin == null) {
            return null;
        }

        return ProfileFieldNormalizer.firstNonBlank(
                readAdminField(admin, "getProfileFullName"),
                join(readAdminField(admin, "getName"), readAdminField(admin, "getSurname"))
        );
    }

    private String readAdminEmail(Admin admin) {
        return readAdminField(admin, "getEmailAddress");
    }

    private String readAdminPhone(Admin admin) {
        return readAdminField(admin, "getMobileNumber");
    }

    private String readAdminField(Admin admin, String methodName) {
        if (admin == null || methodName == null || methodName.isBlank()) {
            return null;
        }

        try {
            Object value = admin.getClass().getMethod(methodName).invoke(admin);
            return value == null ? null : value.toString().trim();
        } catch (Exception ex) {
            return null;
        }
    }

    private String join(String first, String last) {
        String firstValue = first == null ? "" : first.trim();
        String lastValue = last == null ? "" : last.trim();
        String combined = (firstValue + " " + lastValue).trim();
        return combined.isEmpty() ? null : combined;
    }
}