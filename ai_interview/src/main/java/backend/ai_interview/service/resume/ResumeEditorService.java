package backend.ai_interview.service.resume;

import backend.ai_interview.dto.request.ResumeContentUpdateRequest;
import backend.ai_interview.dto.request.ResumeSectionUpdateRequest;
import backend.ai_interview.dto.response.ResumeEditorResponse;
import backend.ai_interview.entity.Resume;
import backend.ai_interview.entity.ResumeSection;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.exception.ResumeEditingException;
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
 * Resume Editor Service
 *
 * Handles:
 * - loading editor data for a resume version
 * - updating full structured content
 * - updating one section independently
 * - admin-side read access for editor data
 *
 * NOTE:
 * This service stores structured content JSON inside ResumeVersion and
 * also keeps ResumeSection records in sync for section-wise editing.
 *
 * Latest project alignment:
 * - stays consistent with resume version editor endpoints
 * - keeps preview flow updated after content edits
 * - preserves section-wise editing support
 * - keeps resume version data compatible with profile snapshot / format metadata flows
 */
@Service
@SuppressWarnings("all")
public class ResumeEditorService {

    private final ResumeVersionRepository resumeVersionRepository;
    private final ResumeSectionRepository resumeSectionRepository;
    private final ObjectMapper objectMapper;

    public ResumeEditorService(
            ResumeVersionRepository resumeVersionRepository,
            ResumeSectionRepository resumeSectionRepository,
            ObjectMapper objectMapper
    ) {
        this.resumeVersionRepository = resumeVersionRepository;
        this.resumeSectionRepository = resumeSectionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Load editor data for a user's resume version.
     */
    @Transactional(readOnly = true)
    public ResumeEditorResponse getEditorData(String userId, Long versionId) {
        validateUser(userId);
        ResumeVersion version = findUserVersion(userId, versionId);
        return toEditorResponse(version, "Resume editor data fetched successfully");
    }

    /**
     * Load editor data for admin inspection.
     */
    @Transactional(readOnly = true)
    public ResumeEditorResponse getEditorDataForAdmin(Long versionId) {
        ResumeVersion version = findVersion(versionId);
        return toEditorResponse(version, "Resume editor data fetched successfully");
    }

    /**
     * Replace full structured content of a version.
     */
    @Transactional
    public ResumeEditorResponse updateEditorData(
            String userId,
            Long versionId,
            ResumeContentUpdateRequest request
    ) {
        validateUser(userId);

        if (request == null) {
            throw new ResumeEditingException("Resume content update request cannot be null");
        }
        if (request.getStructuredContent() == null || request.getStructuredContent().isEmpty()) {
            throw new ResumeEditingException("Structured content is required");
        }

        ResumeVersion version = findUserVersion(userId, versionId);

        try {
            Map<String, Object> normalizedContent = normalizeStructuredContent(request.getStructuredContent());

            version.setStructuredContentJson(writeJson(normalizedContent));

            if (request.getRawText() != null) {
                version.setRawText(trimToNull(request.getRawText()));
            } else {
                version.setRawText(trimToNull(buildRawTextFromStructuredContent(normalizedContent)));
            }

            if (request.shouldRegeneratePreview()) {
                version.setPreviewUrl(buildPreviewUrl(version));
            }

            // Keep latest project fields stable if already present.
            version.setProfileSnapshotJson(trimToNull(version.getProfileSnapshotJson()));
            version.setFormatMetadataJson(trimToNull(version.getFormatMetadataJson()));

            resumeVersionRepository.save(version);

            syncSectionsFromStructuredContent(version, normalizedContent);

            return toEditorResponse(version, "Resume content updated successfully");

        } catch (ResumeEditingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResumeEditingException("Failed to update resume content", ex);
        }
    }

    /**
     * Update one section of a resume version.
     */
    @Transactional
    public ResumeEditorResponse updateSection(
            String userId,
            Long versionId,
            ResumeSectionUpdateRequest request
    ) {
        validateUser(userId);

        if (request == null) {
            throw new ResumeEditingException("Resume section update request cannot be null");
        }
        if (isBlank(request.getSectionType())) {
            throw new ResumeEditingException("Section type is required");
        }
        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new ResumeEditingException("Section content is required");
        }

        ResumeVersion version = findUserVersion(userId, versionId);

        try {
            String normalizedSectionType = request.getSectionType().trim().toUpperCase(Locale.ROOT);

            ResumeSection section = resumeSectionRepository
                    .findByResumeVersion_ResumeVersionIdAndSectionType(versionId, normalizedSectionType)
                    .orElseGet(() -> {
                        ResumeSection newSection = new ResumeSection();
                        newSection.setResumeVersion(version);
                        newSection.setSectionType(normalizedSectionType);
                        return newSection;
                    });

            section.setSectionTitle(
                    isBlank(request.getSectionTitle())
                            ? toDisplayTitle(normalizedSectionType)
                            : request.getSectionTitle().trim()
            );
            section.setSectionOrder(request.getSectionOrder() == null ? 0 : Math.max(0, request.getSectionOrder()));
            section.setPlainText(
                    isBlank(request.getPlainText())
                            ? trimToNull(buildPlainTextFromSectionContent(request.getContent()))
                            : trimToNull(request.getPlainText())
            );
            section.setContentJson(writeJson(request.getContent()));

            resumeSectionRepository.save(section);

            Map<String, Object> structuredContent = readStructuredContent(version.getStructuredContentJson());
            putSectionIntoStructuredContent(
                    structuredContent,
                    normalizedSectionType,
                    request.getContent(),
                    section.getPlainText()
            );

            version.setStructuredContentJson(writeJson(structuredContent));
            version.setRawText(trimToNull(buildRawTextFromStructuredContent(structuredContent)));
            version.setPreviewUrl(buildPreviewUrl(version));

            // Preserve latest project metadata fields.
            version.setProfileSnapshotJson(trimToNull(version.getProfileSnapshotJson()));
            version.setFormatMetadataJson(trimToNull(version.getFormatMetadataJson()));

            resumeVersionRepository.save(version);

            return toEditorResponse(version, "Resume section updated successfully");

        } catch (ResumeEditingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResumeEditingException("Failed to update resume section", ex);
        }
    }

    private ResumeVersion findUserVersion(String userId, Long versionId) {
        if (versionId == null) {
            throw new ResumeEditingException("Resume version id is required");
        }

        return resumeVersionRepository.findByResumeVersionIdAndResume_User_UserId(versionId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found"));
    }

    private ResumeVersion findVersion(Long versionId) {
        if (versionId == null) {
            throw new ResumeEditingException("Resume version id is required");
        }

        return resumeVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume version not found"));
    }

    private ResumeEditorResponse toEditorResponse(ResumeVersion version, String message) {
        Resume resume = version.getResume();
        Map<String, Object> structuredContent = readStructuredContent(version.getStructuredContentJson());

        if (structuredContent.isEmpty()) {
            structuredContent = buildStructuredContentFromSections(version);
        }

        ResumeEditorResponse response = ResumeEditorResponse.of(
                resume != null ? resume.getResumeId() : null,
                version.getResumeVersionId(),
                resume != null ? resume.getResumeCode() : null,
                version.getVersionCode(),
                version.getVersionName(),
                version.getVersionType(),
                structuredContent,
                version.getRawText(),
                version.getPreviewUrl(),
                version.getAtsScore(),
                version.getCreatedAt(),
                version.getUpdatedAt()
        );

        response.setMessage(message);
        return response;
    }

    private Map<String, Object> buildStructuredContentFromSections(ResumeVersion version) {
        List<ResumeSection> sections = resumeSectionRepository
                .findByResumeVersion_ResumeVersionIdOrderBySectionOrderAsc(version.getResumeVersionId());

        Map<String, Object> structuredContent = new LinkedHashMap<>();

        for (ResumeSection section : sections) {
            if (section == null || isBlank(section.getSectionType())) {
                continue;
            }

            String key = toStructuredContentKey(section.getSectionType());
            Map<String, Object> contentMap = readJsonMap(section.getContentJson());

            if (!contentMap.isEmpty()) {
                structuredContent.put(key, contentMap);
            } else if (!isBlank(section.getPlainText())) {
                structuredContent.put(key, section.getPlainText().trim());
            }
        }

        return structuredContent;
    }

    private void syncSectionsFromStructuredContent(ResumeVersion version, Map<String, Object> structuredContent) {
        resumeSectionRepository.deleteByResumeVersion_ResumeVersionId(version.getResumeVersionId());

        int order = 1;
        for (Map.Entry<String, Object> entry : structuredContent.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String sectionType = toSectionType(key);
            String plainText = trimToNull(buildPlainText(value));

            Map<String, Object> contentMap;
            if (value instanceof Map<?, ?> mapValue) {
                contentMap = new LinkedHashMap<>();
                for (Map.Entry<?, ?> mapEntry : mapValue.entrySet()) {
                    if (mapEntry.getKey() != null) {
                        contentMap.put(String.valueOf(mapEntry.getKey()), mapEntry.getValue());
                    }
                }
            } else if (value instanceof List<?>) {
                contentMap = new LinkedHashMap<>();
                contentMap.put("items", value);
            } else {
                contentMap = new LinkedHashMap<>();
                contentMap.put("value", value);
            }

            ResumeSection section = new ResumeSection();
            section.setResumeVersion(version);
            section.setSectionType(sectionType);
            section.setSectionTitle(toDisplayTitle(sectionType));
            section.setSectionOrder(order++);
            section.setContentJson(writeJson(contentMap));
            section.setPlainText(plainText);

            resumeSectionRepository.save(section);
        }
    }

    private void putSectionIntoStructuredContent(
            Map<String, Object> structuredContent,
            String sectionType,
            Map<String, Object> sectionContent,
            String plainText
    ) {
        String key = toStructuredContentKey(sectionType);

        if (sectionContent != null && !sectionContent.isEmpty()) {
            structuredContent.put(key, new LinkedHashMap<>(sectionContent));
        } else if (!isBlank(plainText)) {
            structuredContent.put(key, plainText.trim());
        }
    }

    private Map<String, Object> normalizeStructuredContent(Map<String, Object> content) {
        Map<String, Object> normalized = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : content.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            String key = entry.getKey().trim();
            if (key.isEmpty()) {
                continue;
            }

            Object value = entry.getValue();
            normalized.put(key, value);
        }

        return normalized;
    }

    private Map<String, Object> readStructuredContent(String json) {
        return readJsonMap(json);
    }

    private Map<String, Object> readJsonMap(String json) {
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

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new ResumeEditingException("Failed to serialize resume content", ex);
        }
    }

    private String buildRawTextFromStructuredContent(Map<String, Object> structuredContent) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, Object> entry : structuredContent.entrySet()) {
            String body = buildPlainText(entry.getValue());

            if (body.isBlank()) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append(toDisplayTitle(toSectionType(entry.getKey()))).append("\n");
            builder.append(body);
        }

        return builder.toString().trim();
    }

    private String buildPlainTextFromSectionContent(Map<String, Object> sectionContent) {
        return buildPlainText(sectionContent);
    }

    private String buildPlainText(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String stringValue) {
            return stringValue.trim();
        }

        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }

        if (value instanceof List<?> listValue) {
            StringBuilder builder = new StringBuilder();
            for (Object item : listValue) {
                String itemText = buildPlainText(item);
                if (!itemText.isBlank()) {
                    if (builder.length() > 0) {
                        builder.append("\n");
                    }
                    builder.append("- ").append(itemText);
                }
            }
            return builder.toString().trim();
        }

        if (value instanceof Map<?, ?> mapValue) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                String key = entry.getKey() == null ? "" : String.valueOf(entry.getKey()).trim();
                String entryValue = buildPlainText(entry.getValue());

                if (entryValue.isBlank()) {
                    continue;
                }

                if (builder.length() > 0) {
                    builder.append("\n");
                }

                if (!key.isBlank()) {
                    builder.append(key).append(": ").append(entryValue);
                } else {
                    builder.append(entryValue);
                }
            }
            return builder.toString().trim();
        }

        return String.valueOf(value).trim();
    }

    private String buildPreviewUrl(ResumeVersion version) {
        return "/api/user/resume/version/" + version.getResumeVersionId() + "/preview";
    }

    private String toStructuredContentKey(String sectionType) {
        String normalized = sectionType == null ? "" : sectionType.trim().toLowerCase(Locale.ROOT);
        return normalized.replace('_', ' ');
    }

    private String toSectionType(String key) {
        return key == null
                ? "GENERAL"
                : key.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private String toDisplayTitle(String sectionType) {
        String normalized = sectionType == null
                ? "GENERAL"
                : sectionType.trim().replace('_', ' ').toLowerCase(Locale.ROOT);

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

    private void validateUser(String userId) {
        if (isBlank(userId)) {
            throw new ResumeEditingException("Invalid user session. Please login again.");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}