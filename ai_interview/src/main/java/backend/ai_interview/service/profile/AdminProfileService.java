package backend.ai_interview.service.profile;

import backend.ai_interview.dto.request.AdminProfileUpdateRequest;
import backend.ai_interview.dto.request.ProfilePreferenceUpdateRequest;
import backend.ai_interview.dto.response.AdminProfileResponse;
import backend.ai_interview.dto.response.ProfileSummaryResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AdminProfile;
import backend.ai_interview.entity.enums.ProfileSourceType;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.exception.ProfileNotFoundException;
import backend.ai_interview.repository.AdminProfileRepository;
import backend.ai_interview.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AdminProfileService
 *
 * Handles:
 * - loading logged-in admin's official profile
 * - creating default profile if missing
 * - updating admin profile manually
 * - loading navbar summary
 * - updating profile preferences
 *
 * Latest project update:
 * - supports navbar-based profile module
 * - supports database-backed admin profile storage
 * - keeps Admin and AdminProfile data aligned
 * - stays compatible with evolving admin entity/profile structure
 */
@Service
@SuppressWarnings("all")
public class AdminProfileService {

    private final AdminRepository adminRepository;
    private final AdminProfileRepository adminProfileRepository;

    public AdminProfileService(
            AdminRepository adminRepository,
            AdminProfileRepository adminProfileRepository
    ) {
        this.adminRepository = adminRepository;
        this.adminProfileRepository = adminProfileRepository;
    }

    /**
     * Fetch logged-in admin's full profile.
     */
    @Transactional(readOnly = true)
    public AdminProfileResponse getMyProfile(String adminId) {
        validateAdminId(adminId);

        AdminProfile profile = getOrCreateProfile(adminId);
        return toResponse(profile, "Admin profile fetched successfully");
    }

    /**
     * Update logged-in admin's profile.
     */
    @Transactional
    public AdminProfileResponse updateMyProfile(String adminId, AdminProfileUpdateRequest request) {
        validateAdminId(adminId);

        if (request == null) {
            throw new ApiException("Invalid profile update request.");
        }

        AdminProfile profile = getOrCreateProfile(adminId);

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
        if (request.getTopSkillsJson() != null) {
            profile.setTopSkillsJson(trimToNull(request.getTopSkillsJson()));
        }

        profile.setProfileSourceType(resolveUpdatedSourceType(profile.getProfileSourceType()));

        profile = adminProfileRepository.save(profile);
        syncAdminFromProfile(profile);

        return toResponse(profile, "Admin profile updated successfully");
    }

    /**
     * Fetch compact navbar summary.
     */
    @Transactional(readOnly = true)
    public ProfileSummaryResponse getNavbarSummary(String adminId) {
        validateAdminId(adminId);

        AdminProfile profile = getOrCreateProfile(adminId);

        ProfileSummaryResponse response = ProfileSummaryResponse.ok("Profile summary fetched successfully");
        response.setAdminId(profile.getAdmin() != null ? profile.getAdmin().getAdminId() : null);
        response.setFullName(firstNonBlank(
                profile.getFullName(),
                buildFullName(profile.getAdmin())
        ));
        response.setHeadline(firstNonBlank(profile.getPreferredHeadline(), profile.getHeadline(), ""));
        response.setEmail(firstNonBlank(
                profile.getEmail(),
                profile.getAdmin() != null ? profile.getAdmin().getEmailAddress() : null,
                ""
        ));

        return response;
    }

    /**
     * Optional helper for profile preferences.
     */
    @Transactional
    public AdminProfileResponse updatePreferences(String adminId, ProfilePreferenceUpdateRequest request) {
        validateAdminId(adminId);

        if (request == null) {
            throw new ApiException("Invalid profile preference update request.");
        }

        AdminProfile profile = getOrCreateProfile(adminId);

        if (request.getAutoSyncFromResume() != null) {
            profile.setAutoSyncFromResume(request.getAutoSyncFromResume());
        }
        if (request.getAllowResumeOverwrite() != null) {
            profile.setAllowResumeOverwrite(request.getAllowResumeOverwrite());
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

        profile = adminProfileRepository.save(profile);
        return toResponse(profile, "Profile preferences updated successfully");
    }

    /**
     * Get existing profile or create a default one from Admin.
     */
    @Transactional
    public AdminProfile getOrCreateProfile(String adminId) {
        validateAdminId(adminId);

        return adminProfileRepository.findByAdmin_AdminId(adminId)
                .orElseGet(() -> {
                    Admin admin = adminRepository.findByAdminId(adminId)
                            .orElseThrow(() -> new ProfileNotFoundException("Admin not found: " + adminId));

                    AdminProfile profile = AdminProfile.builder()
                            .admin(admin)
                            .fullName(buildFullName(admin))
                            .email(normalizeEmail(admin.getEmailAddress()))
                            .phone(trimToNull(admin.getMobileNumber()))
                            .profileSourceType(ProfileSourceType.MANUAL.name())
                            .build();

                    return adminProfileRepository.save(profile);
                });
    }

    /**
     * Convert entity to response.
     */
    private AdminProfileResponse toResponse(AdminProfile profile, String message) {
        if (profile == null) {
            return AdminProfileResponse.fail("Admin profile not found.");
        }

        AdminProfileResponse response = AdminProfileResponse.ok(message);
        response.setAdminId(profile.getAdmin() != null ? profile.getAdmin().getAdminId() : null);
        response.setFullName(profile.getFullName());
        response.setEmail(profile.getEmail());
        response.setPhone(profile.getPhone());
        response.setHeadline(firstNonBlank(profile.getPreferredHeadline(), profile.getHeadline(), null));
        response.setLocation(firstNonBlank(profile.getPreferredLocation(), profile.getLocation(), null));
        response.setLinkedinUrl(profile.getLinkedinUrl());
        response.setGithubUrl(profile.getGithubUrl());
        response.setPortfolioUrl(profile.getPortfolioUrl());
        response.setProfileSummary(profile.getProfileSummary());
        response.setTopSkillsJson(profile.getTopSkillsJson());
        response.setSourceResumeVersionId(profile.getSourceResumeVersionId());
        response.setProfileSourceType(profile.getProfileSourceType());

        return response;
    }

    /**
     * Keep account-level Admin fields aligned with official AdminProfile
     * when such fields exist in the Admin entity.
     */
    private void syncAdminFromProfile(AdminProfile profile) {
        if (profile == null || profile.getAdmin() == null) {
            return;
        }

        Admin admin = profile.getAdmin();

        trySetAdminField(admin, "setProfileFullName", profile.getFullName());
        trySetAdminField(admin, "setProfileHeadline", profile.getHeadline());
        trySetAdminField(admin, "setProfileLocation", profile.getLocation());
        trySetAdminField(admin, "setProfileSummary", profile.getProfileSummary());
        trySetAdminField(admin, "setLinkedinUrl", profile.getLinkedinUrl());
        trySetAdminField(admin, "setGithubUrl", profile.getGithubUrl());
        trySetAdminField(admin, "setPortfolioUrl", profile.getPortfolioUrl());
        trySetAdminField(admin, "setTopSkillsJson", profile.getTopSkillsJson());
        trySetAdminField(admin, "setProfileSourceType", profile.getProfileSourceType());
        trySetAdminField(admin, "setSourceResumeVersionId", profile.getSourceResumeVersionId());

        adminRepository.save(admin);
    }

    /**
     * Reflection-based safe setter so this service compiles
     * even if your Admin entity is not yet fully expanded
     * with all profile fields.
     */
    private void trySetAdminField(Admin admin, String methodName, Object value) {
        try {
            if (value == null) {
                Class<?> paramType = findSingleParamType(admin, methodName);
                if (paramType != null) {
                    admin.getClass().getMethod(methodName, paramType).invoke(admin, new Object[]{null});
                }
                return;
            }

            Class<?> valueType = value.getClass();
            try {
                admin.getClass().getMethod(methodName, valueType).invoke(admin, value);
            } catch (NoSuchMethodException ex) {
                Class<?> paramType = findSingleParamType(admin, methodName);
                if (paramType != null) {
                    admin.getClass().getMethod(methodName, paramType).invoke(admin, value);
                }
            }
        } catch (Exception ignored) {
            // Intentionally ignored to preserve compatibility
            // with older Admin entity versions.
        }
    }

    private Class<?> findSingleParamType(Admin admin, String methodName) {
        return java.util.Arrays.stream(admin.getClass().getMethods())
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> method.getParameterCount() == 1)
                .map(method -> method.getParameterTypes()[0])
                .findFirst()
                .orElse(null);
    }

    private void validateAdminId(String adminId) {
        if (isBlank(adminId)) {
            throw new ApiException("Invalid admin session. Please login again.");
        }
    }

    private String buildFullName(Admin admin) {
        if (admin == null) {
            return null;
        }

        String first = null;
        String last = null;

        try {
            first = trimToEmpty((String) admin.getClass().getMethod("getName").invoke(admin));
        } catch (Exception ignored) {
        }

        try {
            last = trimToEmpty((String) admin.getClass().getMethod("getSurname").invoke(admin));
        } catch (Exception ignored) {
        }

        String full = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
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