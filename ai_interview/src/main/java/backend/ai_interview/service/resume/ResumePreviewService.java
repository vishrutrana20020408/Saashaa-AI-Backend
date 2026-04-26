package backend.ai_interview.service.resume;

import backend.ai_interview.dto.response.ResumePreviewResponse;
import backend.ai_interview.dto.response.ResumeProfileSnapshotResponse;
import backend.ai_interview.entity.Resume;
import backend.ai_interview.entity.ResumeSection;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.exception.ResumeNotFoundException;
import backend.ai_interview.repository.ResumeSectionRepository;
import backend.ai_interview.repository.ResumeVersionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Resume Preview Service
 *
 * Handles:
 * - user-side preview for a resume version
 * - admin-side preview for a resume version
 * - rendering preview content from structured JSON / sections
 *
 * NOTE:
 * This is a starter implementation that renders HTML preview content
 * from stored structured resume content.
 *
 * Latest project update:
 * - includes parsed profile snapshot in preview response
 * - includes resume format metadata in preview response
 * - supports resume file + extracted profile + format-aware preview flow
 * - stays aligned with resume editor / version / profile sync continuity
 */
@Service
@SuppressWarnings("all")
public class ResumePreviewService {

    private final ResumeVersionRepository resumeVersionRepository;
    private final ResumeSectionRepository resumeSectionRepository;
    private final ObjectMapper objectMapper;

    public ResumePreviewService(
            ResumeVersionRepository resumeVersionRepository,
            ResumeSectionRepository resumeSectionRepository,
            ObjectMapper objectMapper
    ) {
        this.resumeVersionRepository = resumeVersionRepository;
        this.resumeSectionRepository = resumeSectionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get preview for a user's resume version.
     */
    @Transactional(readOnly = true)
    public ResumePreviewResponse getPreview(String userId, Long versionId) {
        validateUser(userId);

        if (versionId == null) {
            throw new ResumeNotFoundException("Resume version id is required");
        }

        ResumeVersion version = resumeVersionRepository
                .findByResumeVersionIdAndResume_User_UserId(versionId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found"));

        return buildPreviewResponse(version, "Resume preview fetched successfully");
    }

    /**
     * Get preview for admin inspection.
     */
    @Transactional(readOnly = true)
    public ResumePreviewResponse getPreviewForAdmin(Long versionId) {
        if (versionId == null) {
            throw new ResumeNotFoundException("Resume version id is required");
        }

        ResumeVersion version = resumeVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found"));

        return buildPreviewResponse(version, "Resume preview fetched successfully");
    }

    private ResumePreviewResponse buildPreviewResponse(ResumeVersion version, String message) {
        Resume resume = version.getResume();
        Map<String, Object> structuredContent = readStructuredContent(version.getStructuredContentJson());

        if (structuredContent.isEmpty()) {
            structuredContent = buildStructuredContentFromSections(version.getResumeVersionId());
        }

        ResumeProfileSnapshotResponse profileSnapshot = readProfileSnapshot(
                version.getProfileSnapshotJson(),
                resume != null ? resume.getResumeId() : null,
                version.getResumeVersionId()
        );

        Map<String, Object> formatMetadata = readMap(version.getFormatMetadataJson());

        String previewContent = renderPreviewHtml(structuredContent, version, profileSnapshot, formatMetadata);

        Map<String, Object> previewData = new LinkedHashMap<>();
        previewData.put("structuredContent", structuredContent);
        previewData.put("sectionCount", structuredContent.size());
        previewData.put("hasRawText", version.getRawText() != null && !version.getRawText().isBlank());
        previewData.put("hasProfileSnapshot", profileSnapshot != null);
        previewData.put("hasFormatMetadata", formatMetadata != null && !formatMetadata.isEmpty());
        previewData.put("isBaseVersion", Boolean.TRUE.equals(version.getBaseVersion()));
        previewData.put("hasPreviewUrl", !isBlank(version.getPreviewUrl()));
        previewData.put("hasFileUrl", !isBlank(version.getFileUrl()));

        ResumePreviewResponse response = ResumePreviewResponse.of(
                resume != null ? resume.getResumeId() : null,
                version.getResumeVersionId(),
                resume != null ? resume.getResumeCode() : null,
                version.getVersionCode(),
                version.getVersionName(),
                version.getVersionType(),
                previewContent,
                previewData,
                firstNonBlank(version.getPreviewUrl(), buildPreviewUrl(version)),
                buildDownloadUrl(version),
                version.getFileUrl(),
                version.getAtsScore(),
                profileSnapshot,
                formatMetadata,
                version.getCreatedAt(),
                version.getUpdatedAt()
        );

        response.setMessage(message);
        return response;
    }

    private Map<String, Object> buildStructuredContentFromSections(Long versionId) {
        List<ResumeSection> sections =
                resumeSectionRepository.findByResumeVersion_ResumeVersionIdOrderBySectionOrderAsc(versionId);

        Map<String, Object> structuredContent = new LinkedHashMap<>();

        for (ResumeSection section : sections) {
            if (section == null || isBlank(section.getSectionType())) {
                continue;
            }

            String key = toStructuredContentKey(section.getSectionType());
            Map<String, Object> contentMap = readMap(section.getContentJson());

            if (!contentMap.isEmpty()) {
                structuredContent.put(key, contentMap);
            } else if (!isBlank(section.getPlainText())) {
                structuredContent.put(key, section.getPlainText().trim());
            }
        }

        return structuredContent;
    }

    private Map<String, Object> readStructuredContent(String json) {
        return readMap(json);
    }

    private Map<String, Object> readMap(String json) {
        if (isBlank(json)) {
            return new LinkedHashMap<>();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<LinkedHashMap<String, Object>>() {}
            );
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private ResumeProfileSnapshotResponse readProfileSnapshot(String json, Long resumeId, Long versionId) {
        ResumeProfileSnapshotResponse snapshot;

        if (isBlank(json)) {
            snapshot = null;
        } else {
            try {
                snapshot = objectMapper.readValue(json, ResumeProfileSnapshotResponse.class);
            } catch (Exception ex) {
                snapshot = null;
            }
        }

        if (snapshot != null) {
            if (snapshot.getResumeId() == null) {
                snapshot.setResumeId(resumeId);
            }
            if (snapshot.getVersionId() == null) {
                snapshot.setVersionId(versionId);
            }
            if (!snapshot.isSuccess()) {
                snapshot.setSuccess(true);
            }
            if (isBlank(snapshot.getMessage())) {
                snapshot.setMessage("Resume profile snapshot loaded successfully");
            }
        }

        return snapshot;
    }

    private String renderPreviewHtml(
            Map<String, Object> structuredContent,
            ResumeVersion version,
            ResumeProfileSnapshotResponse profileSnapshot,
            Map<String, Object> formatMetadata
    ) {
        StringBuilder html = new StringBuilder();

        html.append("<div class=\"resume-preview\">");

        html.append("<div class=\"resume-preview-header\">");
        html.append("<h1>").append(escapeHtml(version.getVersionName())).append("</h1>");
        html.append("<p><strong>Version Type:</strong> ")
                .append(escapeHtml(version.getVersionType()))
                .append("</p>");

        if (version.getAtsScore() != null) {
            html.append("<p><strong>ATS Score:</strong> ")
                    .append(version.getAtsScore())
                    .append("</p>");
        }

        if (!isBlank(version.getFileUrl())) {
            html.append("<p><strong>File URL:</strong> ")
                    .append(escapeHtml(version.getFileUrl()))
                    .append("</p>");
        }

        if (!isBlank(version.getPreviewUrl())) {
            html.append("<p><strong>Preview URL:</strong> ")
                    .append(escapeHtml(version.getPreviewUrl()))
                    .append("</p>");
        }

        html.append("</div>");

        if (profileSnapshot != null) {
            html.append("<section class=\"resume-section resume-profile-snapshot\">");
            html.append("<h2>Parsed Profile Snapshot</h2>");
            html.append("<div class=\"resume-block\">");

            appendRowIfPresent(html, "Full Name", profileSnapshot.getFullName());
            appendRowIfPresent(html, "Email", profileSnapshot.getEmail());
            appendRowIfPresent(html, "Phone", profileSnapshot.getPhone());
            appendRowIfPresent(html, "Location", profileSnapshot.getLocation());
            appendRowIfPresent(html, "Headline", profileSnapshot.getHeadline());
            appendRowIfPresent(html, "Summary", profileSnapshot.getProfileSummary());
            appendRowIfPresent(html, "LinkedIn", profileSnapshot.getLinkedinUrl());
            appendRowIfPresent(html, "GitHub", profileSnapshot.getGithubUrl());
            appendRowIfPresent(html, "Portfolio", profileSnapshot.getPortfolioUrl());
            appendRowIfPresent(html, "Current Company", profileSnapshot.getCurrentCompany());
            appendRowIfPresent(html, "Current Role", profileSnapshot.getCurrentRole());
            appendRowIfPresent(html, "Highest Education", profileSnapshot.getHighestEducation());

            html.append("</div>");
            html.append("</section>");
        }

        if (formatMetadata != null && !formatMetadata.isEmpty()) {
            html.append("<section class=\"resume-section resume-format-metadata\">");
            html.append("<h2>Resume Format Metadata</h2>");
            html.append(renderValue(formatMetadata));
            html.append("</section>");
        }

        for (Map.Entry<String, Object> entry : structuredContent.entrySet()) {
            String title = toDisplayTitle(entry.getKey());
            Object value = entry.getValue();

            html.append("<section class=\"resume-section\">");
            html.append("<h2>").append(escapeHtml(title)).append("</h2>");
            html.append(renderValue(value));
            html.append("</section>");
        }

        if (structuredContent.isEmpty() && !isBlank(version.getRawText())) {
            html.append("<section class=\"resume-section\">");
            html.append("<h2>Resume Content</h2>");
            html.append("<pre>").append(escapeHtml(version.getRawText())).append("</pre>");
            html.append("</section>");
        }

        html.append("</div>");
        return html.toString();
    }

    private void appendRowIfPresent(StringBuilder html, String label, String value) {
        if (isBlank(value)) {
            return;
        }

        html.append("<div class=\"resume-row\">");
        html.append("<strong>")
                .append(escapeHtml(label))
                .append(":</strong> ")
                .append(escapeHtml(value));
        html.append("</div>");
    }

    private String renderValue(Object value) {
        if (value == null) {
            return "<p></p>";
        }

        if (value instanceof String stringValue) {
            return "<p>" + escapeHtml(stringValue).replace("\n", "<br/>") + "</p>";
        }

        if (value instanceof Number || value instanceof Boolean) {
            return "<p>" + escapeHtml(String.valueOf(value)) + "</p>";
        }

        if (value instanceof List<?> listValue) {
            StringBuilder html = new StringBuilder();
            html.append("<ul>");
            for (Object item : listValue) {
                html.append("<li>").append(renderInlineValue(item)).append("</li>");
            }
            html.append("</ul>");
            return html.toString();
        }

        if (value instanceof Map<?, ?> mapValue) {
            StringBuilder html = new StringBuilder();
            html.append("<div class=\"resume-block\">");
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                String key = entry.getKey() == null ? "" : String.valueOf(entry.getKey());
                Object entryValue = entry.getValue();

                html.append("<div class=\"resume-row\">");
                if (!key.isBlank()) {
                    html.append("<strong>")
                            .append(escapeHtml(toDisplayTitle(key)))
                            .append(":</strong> ");
                }
                html.append(renderInlineValue(entryValue));
                html.append("</div>");
            }
            html.append("</div>");
            return html.toString();
        }

        return "<p>" + escapeHtml(String.valueOf(value)) + "</p>";
    }

    private String renderInlineValue(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String stringValue) {
            return escapeHtml(stringValue).replace("\n", "<br/>");
        }

        if (value instanceof Number || value instanceof Boolean) {
            return escapeHtml(String.valueOf(value));
        }

        if (value instanceof List<?> listValue) {
            StringBuilder html = new StringBuilder();
            html.append("<ul>");
            for (Object item : listValue) {
                html.append("<li>").append(renderInlineValue(item)).append("</li>");
            }
            html.append("</ul>");
            return html.toString();
        }

        if (value instanceof Map<?, ?> mapValue) {
            StringBuilder html = new StringBuilder();
            html.append("<div>");
            boolean first = true;

            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                if (!first) {
                    html.append("<br/>");
                }
                first = false;

                String key = entry.getKey() == null ? "" : String.valueOf(entry.getKey());
                if (!key.isBlank()) {
                    html.append("<strong>")
                            .append(escapeHtml(toDisplayTitle(key)))
                            .append(":</strong> ");
                }
                html.append(renderInlineValue(entry.getValue()));
            }

            html.append("</div>");
            return html.toString();
        }

        return escapeHtml(String.valueOf(value));
    }

    private String buildDownloadUrl(ResumeVersion version) {
        return "/api/user/resume/version/" + version.getResumeVersionId() + "/download";
    }

    private String buildPreviewUrl(ResumeVersion version) {
        return "/api/user/resume/version/" + version.getResumeVersionId() + "/preview";
    }

    private String toStructuredContentKey(String sectionType) {
        return sectionType == null
                ? "general"
                : sectionType.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private String toDisplayTitle(String key) {
        String normalized = key == null
                ? "General"
                : key.trim().replace('_', ' ').toLowerCase(Locale.ROOT);

        String[] parts = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }

        return builder.toString();
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

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void validateUser(String userId) {
        if (isBlank(userId)) {
            throw new ResumeNotFoundException("Invalid user session. Please login again.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}