package backend.ai_interview.service.resume;

import backend.ai_interview.dto.request.ResumeDuplicateCreateRequest;
import backend.ai_interview.dto.response.ResumeVersionResponse;
import backend.ai_interview.entity.Resume;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.exception.ResumeEditingException;
import backend.ai_interview.exception.ResumeNotFoundException;
import backend.ai_interview.repository.ResumeRepository;
import backend.ai_interview.repository.ResumeVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Resume Version Service
 *
 * Handles:
 * - fetch single version for user/admin
 * - fetch all versions of a resume
 * - create duplicate version from an existing version
 *
 * IMPORTANT DESIGN:
 * - original/base version must remain unchanged
 * - duplicate/tailored versions are created as separate records
 *
 * Latest project alignment:
 * - stays consistent with resume version editor/preview flow
 * - supports duplicate creation for job-specific and tailoring flows
 * - keeps version listing ordered for frontend stability
 */
@Service
@SuppressWarnings("all")
public class ResumeVersionService {

    private static final Logger log = LoggerFactory.getLogger(ResumeVersionService.class);

    private final ResumeVersionRepository resumeVersionRepository;
    private final ResumeRepository resumeRepository;

    public ResumeVersionService(
            ResumeVersionRepository resumeVersionRepository,
            ResumeRepository resumeRepository
    ) {
        this.resumeVersionRepository = resumeVersionRepository;
        this.resumeRepository = resumeRepository;
    }

    /**
     * Fetch one resume version for a specific user.
     */
    @Transactional(readOnly = true)
    public ResumeVersionResponse getVersion(String userId, Long versionId) {
        validateUserId(userId);

        if (versionId == null) {
            throw new ApiException("Resume version id is required");
        }

        ResumeVersion version = resumeVersionRepository
                .findByResumeVersionIdAndResume_User_UserId(versionId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found"));

        return toResponse(version, "Resume version fetched successfully");
    }

    /**
     * Fetch one resume version for admin.
     */
    @Transactional(readOnly = true)
    public ResumeVersionResponse getVersionForAdmin(Long versionId) {
        if (versionId == null) {
            throw new ApiException("Resume version id is required");
        }

        ResumeVersion version = resumeVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found"));

        return toResponse(version, "Resume version fetched successfully");
    }

    /**
     * Fetch all versions of a resume for a specific user.
     */
    @Transactional(readOnly = true)
    public List<ResumeVersionResponse> getVersionsByResume(String userId, Long resumeId) {
        validateUserId(userId);

        if (resumeId == null) {
            throw new ApiException("Resume id is required");
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found"));

        if (resume.getUser() == null || !userId.equals(resume.getUser().getUserId())) {
            throw new ResumeNotFoundException("Resume not found for this user");
        }

        return resumeVersionRepository.findByResume_ResumeIdOrderByCreatedAtDesc(resumeId)
                .stream()
                .map(version -> toResponse(version, "Resume versions fetched successfully"))
                .collect(Collectors.toList());
    }

    /**
     * Fetch all versions of a resume for admin.
     */
    @Transactional(readOnly = true)
    public List<ResumeVersionResponse> getVersionsByResumeForAdmin(Long resumeId) {
        if (resumeId == null) {
            throw new ApiException("Resume id is required");
        }

        if (!resumeRepository.existsById(resumeId)) {
            throw new ResumeNotFoundException("Resume not found");
        }

        return resumeVersionRepository.findByResume_ResumeIdOrderByCreatedAtDesc(resumeId)
                .stream()
                .map(version -> toResponse(version, "Resume versions fetched successfully"))
                .collect(Collectors.toList());
    }

    /**
     * Create a duplicate version from an existing version.
     */
    @Transactional
    public ResumeVersionResponse createDuplicate(String userId, ResumeDuplicateCreateRequest request) {
        validateUserId(userId);

        if (request == null) {
            throw new ResumeEditingException("Resume duplicate request cannot be null");
        }
        if (request.getSourceVersionId() == null) {
            throw new ResumeEditingException("Source resume version id is required");
        }
        if (isBlank(request.getVersionName())) {
            throw new ResumeEditingException("Version name is required");
        }

        ResumeVersion sourceVersion = resumeVersionRepository
                .findByResumeVersionIdAndResume_User_UserId(request.getSourceVersionId(), userId)
                .orElseThrow(() -> new ResumeNotFoundException("Source resume version not found"));

        Resume resume = sourceVersion.getResume();
        if (resume == null) {
            throw new ResumeNotFoundException("Parent resume not found");
        }

        ResumeVersion duplicate = new ResumeVersion();
        duplicate.setResume(resume);
        duplicate.setVersionName(request.getVersionName().trim());
        duplicate.setVersionType(resolveDuplicateVersionType(request));
        duplicate.setBaseVersion(Boolean.FALSE);
        duplicate.setParentVersion(sourceVersion);
        duplicate.setJobApplicationCode(buildJobApplicationCode(request));
        duplicate.setRawText(request.shouldCopyRawText() ? trimToNull(sourceVersion.getRawText()) : null);
        duplicate.setStructuredContentJson(
                request.shouldCopyStructuredContent() ? trimToNull(sourceVersion.getStructuredContentJson()) : null
        );

        // Latest project alignment: keep resume-derived metadata available in duplicates too.
        duplicate.setProfileSnapshotJson(trimToNull(sourceVersion.getProfileSnapshotJson()));
        duplicate.setFormatMetadataJson(trimToNull(sourceVersion.getFormatMetadataJson()));

        duplicate.setFileUrl(trimToNull(sourceVersion.getFileUrl()));
        duplicate.setPreviewUrl(request.shouldGeneratePreview()
                ? firstNonBlank(trimToNull(sourceVersion.getPreviewUrl()), buildPreviewUrlPlaceholder())
                : null);
        duplicate.setAtsScore(sourceVersion.getAtsScore());

        ResumeVersion saved = resumeVersionRepository.save(duplicate);

        long totalVersions = resumeVersionRepository.countByResume_ResumeId(resume.getResumeId());
        resume.setTotalVersions((int) totalVersions);
        resumeRepository.save(resume);

        return toResponse(saved, "Duplicate resume version created successfully");
    }

    private ResumeVersionResponse toResponse(ResumeVersion version, String message) {
        if (version == null) {
            log.warn("toResponse: version is null");
            return ResumeVersionResponse.fail("Resume version not found");
        }

        try {
            Resume resume = version.getResume();
            ResumeVersion parentVersion = version.getParentVersion();

            log.debug("toResponse: versionId={}, resumeId={}, parentId={}",
                    version.getResumeVersionId(),
                    resume != null ? resume.getResumeId() : "null",
                    parentVersion != null ? parentVersion.getResumeVersionId() : "null");

            ResumeVersionResponse response = ResumeVersionResponse.of(
                    version.getResumeVersionId(),
                    version.getVersionCode(),
                    version.getVersionName(),
                    version.getVersionType(),
                    resume != null ? resume.getResumeId() : null,
                    resume != null ? resume.getResumeCode() : null,
                    Boolean.TRUE.equals(version.getBaseVersion()),
                    parentVersion != null ? parentVersion.getResumeVersionId() : null,
                    version.getJobApplicationCode(),
                    version.getFileUrl(),
                    version.getPreviewUrl(),
                    version.getAtsScore(),
                    version.getCreatedAt(),
                    version.getUpdatedAt()
            );
            response.setMessage(message);
            return response;
        } catch (Exception ex) {
            log.error("toResponse: Error while mapping ResumeVersion to ResumeVersionResponse for versionId={}",
                    version.getResumeVersionId(), ex);
            throw ex;
        }
    }

    private String resolveDuplicateVersionType(ResumeDuplicateCreateRequest request) {
        if (!isBlank(request.getCompanyName()) || !isBlank(request.getJobTitle())) {
            return "DUPLICATE";
        }
        return "DUPLICATE";
    }

    private String buildJobApplicationCode(ResumeDuplicateCreateRequest request) {
        String company = normalizeToken(request.getCompanyName());
        String title = normalizeToken(request.getJobTitle());

        if (company.isEmpty() && title.isEmpty()) {
            return null;
        }

        if (company.isEmpty()) {
            return title;
        }

        if (title.isEmpty()) {
            return company;
        }

        return company + "-" + title;
    }

    private String normalizeToken(String value) {
        if (isBlank(value)) {
            return "";
        }

        return value.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String buildPreviewUrlPlaceholder() {
        return "/api/user/resume/version/preview/generated";
    }

    private void validateUserId(String userId) {
        if (isBlank(userId)) {
            throw new ApiException("Invalid user session. Please login again.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}