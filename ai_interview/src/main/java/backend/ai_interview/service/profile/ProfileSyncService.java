package backend.ai_interview.service.profile;

import backend.ai_interview.dto.response.AdminProfileResponse;
import backend.ai_interview.dto.response.ResumeProfileSnapshotResponse;
import backend.ai_interview.dto.response.UserProfileResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AdminProfile;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.entity.UserProfile;
import backend.ai_interview.entity.enums.ProfileSourceType;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.exception.ProfileSyncException;
import backend.ai_interview.exception.ResumeNotFoundException;
import backend.ai_interview.repository.AdminProfileRepository;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.ResumeVersionRepository;
import backend.ai_interview.repository.UserProfileRepository;
import backend.ai_interview.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProfileSyncService
 *
 * Handles syncing official user/admin profiles from parsed resume snapshots.
 *
 * Supported flows:
 * - USER  -> sync own profile from own resume version
 * - ADMIN -> sync own admin profile from a selected user's resume version
 *
 * Notes:
 * - ResumeVersion.profileSnapshotJson is the source of truth for resume-derived profile data
 * - Official profile is stored in UserProfile / AdminProfile
 * - Account-level AppUser profile mirror fields are also updated for navbar/profile use
 *
 * Latest project alignment:
 * - supports resume version preview/editor/profile sync continuity
 * - keeps official profile storage aligned with resume snapshot based flows
 * - preserves compatibility with evolving Admin entity structure
 */
@Service
@SuppressWarnings("all")
public class ProfileSyncService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final UserProfileRepository userProfileRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final ObjectMapper objectMapper;

    public ProfileSyncService(
            UserRepository userRepository,
            AdminRepository adminRepository,
            UserProfileRepository userProfileRepository,
            AdminProfileRepository adminProfileRepository,
            ResumeVersionRepository resumeVersionRepository,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.userProfileRepository = userProfileRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.resumeVersionRepository = resumeVersionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Sync logged-in user's official profile from one of the user's own resume versions.
     */
    @Transactional
    public UserProfileResponse syncUserProfileFromResume(String userId, Long resumeId, Long versionId) {
        validateUserId(userId);

        if (resumeId == null) {
            throw new ApiException("Resume id is required.");
        }
        if (versionId == null) {
            throw new ApiException("Resume version id is required.");
        }

        AppUser user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        ResumeVersion version = resumeVersionRepository
                .findByResume_ResumeIdAndResumeVersionIdAndResume_User_UserId(resumeId, versionId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found."));

        ResumeProfileSnapshotResponse snapshot = readSnapshot(version);

        UserProfile profile = userProfileRepository.findByUser_UserId(userId)
                .orElseGet(() -> createDefaultUserProfile(user));

        applySnapshotToUserProfile(profile, snapshot, version.getResumeVersionId());

        profile = userProfileRepository.save(profile);
        syncAppUserFromProfile(user, profile);

        return buildUserProfileResponse(profile, "Profile synced from resume successfully");
    }

    /**
     * Sync logged-in admin's official profile from a selected user's resume version.
     *
     * This follows the controller contract:
     * POST /api/admin/profile/sync-from-resume/{userId}/versions/{versionId}
     */
    @Transactional
    public AdminProfileResponse syncAdminProfileFromResume(
            String adminId,
            String userId,
            Long versionId
    ) {
        validateAdminId(adminId);

        if (userId == null || userId.isBlank()) {
            throw new ApiException("Target user id is required.");
        }
        if (versionId == null) {
            throw new ApiException("Resume version id is required.");
        }

        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new ApiException("Admin not found."));

        ResumeVersion version = resumeVersionRepository
                .findByResumeVersionIdAndResume_User_UserId(versionId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found."));

        ResumeProfileSnapshotResponse snapshot = readSnapshot(version);

        AdminProfile profile = adminProfileRepository.findByAdmin_AdminId(adminId)
                .orElseGet(() -> createDefaultAdminProfile(admin));

        applySnapshotToAdminProfile(profile, snapshot, version.getResumeVersionId());

        profile = adminProfileRepository.save(profile);
        syncAdminFromProfile(profile);

        return buildAdminProfileResponse(profile, "Admin profile synced from resume successfully");
    }

    private ResumeProfileSnapshotResponse readSnapshot(ResumeVersion version) {
        if (version == null) {
            throw new ProfileSyncException("Profile sync failed: resume version not found.");
        }

        String json = version.getProfileSnapshotJson();
        if (json == null || json.isBlank()) {
            throw ProfileSyncException.missingSnapshot(version.getResumeVersionId());
        }

        try {
            ResumeProfileSnapshotResponse snapshot = objectMapper.readValue(json, ResumeProfileSnapshotResponse.class);

            if (snapshot == null) {
                throw ProfileSyncException.invalidResumeData();
            }

            if (snapshot.getResumeId() == null && version.getResume() != null) {
                snapshot.setResumeId(version.getResume().getResumeId());
            }
            if (snapshot.getVersionId() == null) {
                snapshot.setVersionId(version.getResumeVersionId());
            }
            if (!snapshot.isSuccess()) {
                snapshot.setSuccess(true);
            }
            if (snapshot.getMessage() == null || snapshot.getMessage().isBlank()) {
                snapshot.setMessage("Resume profile snapshot loaded successfully");
            }

            return snapshot;
        } catch (ProfileSyncException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ProfileSyncException("Profile sync failed: unable to read profile snapshot.", ex);
        }
    }

    private UserProfile createDefaultUserProfile(AppUser user) {
        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(firstNonBlank(user.getProfileFullName(), buildUserFullName(user)))
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
                .profileSourceType(ProfileSourceType.MANUAL.name())
                .build();

        return userProfileRepository.save(profile);
    }

    private AdminProfile createDefaultAdminProfile(Admin admin) {
        AdminProfile profile = AdminProfile.builder()
                .admin(admin)
                .fullName(buildAdminFullName(admin))
                .email(normalizeEmail(readAdminString(admin, "getEmailAddress")))
                .phone(trimToNull(readAdminString(admin, "getMobileNumber")))
                .profileSourceType(ProfileSourceType.MANUAL.name())
                .build();

        return adminProfileRepository.save(profile);
    }

    private void applySnapshotToUserProfile(
            UserProfile profile,
            ResumeProfileSnapshotResponse snapshot,
            Long sourceResumeVersionId
    ) {
        profile.setFullName(firstNonBlank(snapshot.getFullName(), profile.getFullName()));
        profile.setEmail(firstNonBlank(normalizeEmail(snapshot.getEmail()), profile.getEmail()));
        profile.setPhone(firstNonBlank(snapshot.getPhone(), profile.getPhone()));
        profile.setHeadline(firstNonBlank(snapshot.getHeadline(), profile.getHeadline()));
        profile.setLocation(firstNonBlank(snapshot.getLocation(), profile.getLocation()));
        profile.setLinkedinUrl(firstNonBlank(snapshot.getLinkedinUrl(), profile.getLinkedinUrl()));
        profile.setGithubUrl(firstNonBlank(snapshot.getGithubUrl(), profile.getGithubUrl()));
        profile.setPortfolioUrl(firstNonBlank(snapshot.getPortfolioUrl(), profile.getPortfolioUrl()));
        profile.setProfileSummary(firstNonBlank(snapshot.getProfileSummary(), profile.getProfileSummary()));
        profile.setCurrentCompany(firstNonBlank(snapshot.getCurrentCompany(), profile.getCurrentCompany()));
        profile.setCurrentRole(firstNonBlank(snapshot.getCurrentRole(), profile.getCurrentRole()));
        profile.setHighestEducation(firstNonBlank(snapshot.getHighestEducation(), profile.getHighestEducation()));
        profile.setTopSkillsJson(firstNonBlank(snapshot.getTopSkillsJson(), profile.getTopSkillsJson()));
        profile.setExperienceSummaryJson(firstNonBlank(snapshot.getExperienceSummaryJson(), profile.getExperienceSummaryJson()));
        profile.setEducationSummaryJson(firstNonBlank(snapshot.getEducationSummaryJson(), profile.getEducationSummaryJson()));
        profile.setProfileSourceType(ProfileSourceType.RESUME.name());
        profile.setSourceResumeVersionId(sourceResumeVersionId);
    }

    private void applySnapshotToAdminProfile(
            AdminProfile profile,
            ResumeProfileSnapshotResponse snapshot,
            Long sourceResumeVersionId
    ) {
        profile.setFullName(firstNonBlank(snapshot.getFullName(), profile.getFullName()));
        profile.setEmail(firstNonBlank(normalizeEmail(snapshot.getEmail()), profile.getEmail()));
        profile.setPhone(firstNonBlank(snapshot.getPhone(), profile.getPhone()));
        profile.setHeadline(firstNonBlank(snapshot.getHeadline(), profile.getHeadline()));
        profile.setLocation(firstNonBlank(snapshot.getLocation(), profile.getLocation()));
        profile.setLinkedinUrl(firstNonBlank(snapshot.getLinkedinUrl(), profile.getLinkedinUrl()));
        profile.setGithubUrl(firstNonBlank(snapshot.getGithubUrl(), profile.getGithubUrl()));
        profile.setPortfolioUrl(firstNonBlank(snapshot.getPortfolioUrl(), profile.getPortfolioUrl()));
        profile.setProfileSummary(firstNonBlank(snapshot.getProfileSummary(), profile.getProfileSummary()));
        profile.setTopSkillsJson(firstNonBlank(snapshot.getTopSkillsJson(), profile.getTopSkillsJson()));
        profile.setProfileSourceType(ProfileSourceType.RESUME.name());
        profile.setSourceResumeVersionId(sourceResumeVersionId);
    }

    private void syncAppUserFromProfile(AppUser user, UserProfile profile) {
        if (user == null || profile == null) {
            return;
        }

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
        user.setProfileSourceType(trimToNull(profile.getProfileSourceType()));
        user.setSourceResumeVersionId(profile.getSourceResumeVersionId());

        userRepository.save(user);
    }

    private void syncAdminFromProfile(AdminProfile profile) {
        if (profile == null || profile.getAdmin() == null) {
            return;
        }

        Admin admin = profile.getAdmin();

        trySetAdminField(admin, "setProfileFullName", trimToNull(profile.getFullName()));
        trySetAdminField(admin, "setProfileHeadline", trimToNull(profile.getHeadline()));
        trySetAdminField(admin, "setProfileLocation", trimToNull(profile.getLocation()));
        trySetAdminField(admin, "setProfileSummary", trimToNull(profile.getProfileSummary()));
        trySetAdminField(admin, "setLinkedinUrl", trimToNull(profile.getLinkedinUrl()));
        trySetAdminField(admin, "setGithubUrl", trimToNull(profile.getGithubUrl()));
        trySetAdminField(admin, "setPortfolioUrl", trimToNull(profile.getPortfolioUrl()));
        trySetAdminField(admin, "setTopSkillsJson", trimToNull(profile.getTopSkillsJson()));
        trySetAdminField(admin, "setProfileSourceType", trimToNull(profile.getProfileSourceType()));
        trySetAdminField(admin, "setSourceResumeVersionId", profile.getSourceResumeVersionId());

        adminRepository.save(admin);
    }

    private UserProfileResponse buildUserProfileResponse(UserProfile profile, String message) {
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
        return response;
    }

    private AdminProfileResponse buildAdminProfileResponse(AdminProfile profile, String message) {
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
            // Preserves compatibility with older Admin entity versions.
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

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ApiException("Invalid user session. Please login again.");
        }
    }

    private void validateAdminId(String adminId) {
        if (adminId == null || adminId.isBlank()) {
            throw new ApiException("Invalid admin session. Please login again.");
        }
    }

    private String buildUserFullName(AppUser user) {
        if (user == null) {
            return null;
        }
        String first = trimToEmpty(user.getName());
        String last = trimToEmpty(user.getSurname());
        String full = (first + " " + last).trim();
        return full.isBlank() ? null : full;
    }

    private String buildAdminFullName(Admin admin) {
        if (admin == null) {
            return null;
        }
        String first = readAdminString(admin, "getName");
        String last = readAdminString(admin, "getSurname");
        String full = (trimToEmpty(first) + " " + trimToEmpty(last)).trim();
        return full.isBlank() ? null : full;
    }

    private String readAdminString(Admin admin, String methodName) {
        try {
            Object value = admin.getClass().getMethod(methodName).invoke(admin);
            return value == null ? null : value.toString().trim();
        } catch (Exception ex) {
            return null;
        }
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
            if (value != null && !value.trim().isEmpty()) {
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
}