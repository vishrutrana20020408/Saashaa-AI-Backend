package backend.ai_interview.service.resume;

import backend.ai_interview.dto.response.ResumeProfileSnapshotResponse;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.exception.ProfileSyncException;
import backend.ai_interview.exception.ResumeNotFoundException;
import backend.ai_interview.repository.ResumeVersionRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ResumeProfileExtractionService
 *
 * Handles extraction and loading of parsed profile snapshot data
 * from resume versions.
 *
 * Responsibilities:
 * - read parsed profile snapshot from ResumeVersion.profileSnapshotJson
 * - generate snapshot from raw/structured resume text when needed
 * - expose helper methods for user/admin profile sync flows
 *
 * Notes:
 * - ResumeVersion.profileSnapshotJson is treated as the primary stored source
 * - ResumeParserService is used as fallback to derive snapshot from raw text
 * - This service does not update official UserProfile/AdminProfile directly
 *
 * Latest project alignment:
 * - supports resume preview/editor/profile sync continuity
 * - stays consistent with resume version based extraction flows
 * - keeps extraction usable for navbar/profile module integration
 */
@Service
@SuppressWarnings("all")
public class ResumeProfileExtractionService {

    private final ResumeVersionRepository resumeVersionRepository;
    private final ResumeParserService resumeParserService;
    private final ObjectMapper objectMapper;

    public ResumeProfileExtractionService(
            ResumeVersionRepository resumeVersionRepository,
            ResumeParserService resumeParserService,
            ObjectMapper objectMapper
    ) {
        this.resumeVersionRepository = resumeVersionRepository;
        this.resumeParserService = resumeParserService;
        this.objectMapper = objectMapper;
    }

    /**
     * Extract profile snapshot for a version owned by a user.
     */
    @Transactional(readOnly = true)
    public ResumeProfileSnapshotResponse extractForUser(String userId, Long versionId) {
        if (userId == null || userId.isBlank()) {
            throw new ProfileSyncException("Invalid user session. Please login again.");
        }
        if (versionId == null) {
            throw new ProfileSyncException("Resume version id is required.");
        }

        ResumeVersion version = resumeVersionRepository
                .findByResumeVersionIdAndResume_User_UserId(versionId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found."));

        return extractFromVersion(version);
    }

    /**
     * Extract profile snapshot for admin inspection.
     */
    @Transactional(readOnly = true)
    public ResumeProfileSnapshotResponse extractForAdmin(Long versionId) {
        if (versionId == null) {
            throw new ProfileSyncException("Resume version id is required.");
        }

        ResumeVersion version = resumeVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found."));

        return extractFromVersion(version);
    }

    /**
     * Extract profile snapshot for a specific resume + version.
     */
    @Transactional(readOnly = true)
    public ResumeProfileSnapshotResponse extractByResumeAndVersion(Long resumeId, Long versionId) {
        if (resumeId == null) {
            throw new ProfileSyncException("Resume id is required.");
        }
        if (versionId == null) {
            throw new ProfileSyncException("Resume version id is required.");
        }

        ResumeVersion version = resumeVersionRepository
                .findByResume_ResumeIdAndResumeVersionId(resumeId, versionId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found."));

        return extractFromVersion(version);
    }

    /**
     * Extract profile snapshot directly from a ResumeVersion.
     */
    @Transactional(readOnly = true)
    public ResumeProfileSnapshotResponse extractFromVersion(ResumeVersion version) {
        if (version == null) {
            throw new ProfileSyncException("Resume version is required.");
        }

        ResumeProfileSnapshotResponse snapshot = tryReadStoredSnapshot(version);
        if (snapshot != null) {
            return snapshot;
        }

        String sourceText = firstNonBlank(
                trimToNull(version.getRawText()),
                stringifyStructuredContent(version.getStructuredContentJson())
        );

        if (sourceText == null || sourceText.isBlank()) {
            throw ProfileSyncException.missingSnapshot(version.getResumeVersionId());
        }

        try {
            ResumeProfileSnapshotResponse extracted = resumeParserService.extractProfileSnapshot(sourceText);
            enrichSnapshotMetadata(extracted, version);
            return extracted;
        } catch (ProfileSyncException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ProfileSyncException("Failed to extract profile snapshot from resume version.", ex);
        }
    }

    /**
     * Extract snapshot as a flexible map, useful for generic controller/service responses.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> extractAsMap(Long versionId) {
        ResumeProfileSnapshotResponse snapshot = extractForAdmin(versionId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", snapshot.isSuccess());
        result.put("message", snapshot.getMessage());
        result.put("resumeId", snapshot.getResumeId());
        result.put("versionId", snapshot.getVersionId());
        result.put("fullName", snapshot.getFullName());
        result.put("email", snapshot.getEmail());
        result.put("phone", snapshot.getPhone());
        result.put("location", snapshot.getLocation());
        result.put("headline", snapshot.getHeadline());
        result.put("profileSummary", snapshot.getProfileSummary());
        result.put("linkedinUrl", snapshot.getLinkedinUrl());
        result.put("githubUrl", snapshot.getGithubUrl());
        result.put("portfolioUrl", snapshot.getPortfolioUrl());
        result.put("currentCompany", snapshot.getCurrentCompany());
        result.put("currentRole", snapshot.getCurrentRole());
        result.put("highestEducation", snapshot.getHighestEducation());
        result.put("topSkillsJson", snapshot.getTopSkillsJson());
        result.put("experienceSummaryJson", snapshot.getExperienceSummaryJson());
        result.put("educationSummaryJson", snapshot.getEducationSummaryJson());

        return result;
    }

    private ResumeProfileSnapshotResponse tryReadStoredSnapshot(ResumeVersion version) {
        String json = version.getProfileSnapshotJson();
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            ResumeProfileSnapshotResponse snapshot =
                    objectMapper.readValue(json, ResumeProfileSnapshotResponse.class);
            enrichSnapshotMetadata(snapshot, version);
            return snapshot;
        } catch (Exception ex) {
            return null;
        }
    }

    private void enrichSnapshotMetadata(ResumeProfileSnapshotResponse snapshot, ResumeVersion version) {
        if (snapshot == null || version == null) {
            return;
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
            snapshot.setMessage("Resume profile snapshot extracted successfully");
        }
    }

    private String stringifyStructuredContent(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }

        try {
            Object value = objectMapper.readValue(json, Object.class);
            if (value == null) {
                return "";
            }
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception ex) {
            return json;
        }
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
}