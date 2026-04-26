package backend.ai_interview.util;

import backend.ai_interview.entity.ResumeSection;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.exception.ResumeEditingException;
import backend.ai_interview.repository.ResumeSectionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Resume Content Mapper
 *
 * Handles:
 * - converting ResumeVersion + ResumeSection entities into structured resume JSON
 * - converting structured resume JSON into ResumeSection entities
 * - merging updates from editor requests
 *
 * This class is used by:
 * - ResumeEditorService
 * - ResumePreviewService
 * - FileGenerationService
 *
 * It acts as the central transformation layer between:
 * DB Entities  <->  Structured Resume Content (Map<String,Object>)
 *
 * Latest project alignment:
 * - keeps structured resume content consistent with editor/preview/file generation flows
 * - preserves compatibility with latest resume version metadata additions
 * - supports profile snapshot / format metadata payload continuity through editor payloads
 */
@Component
@SuppressWarnings("all")
public class ResumeContentMapper {

    private final ResumeSectionRepository resumeSectionRepository;
    private final ObjectMapper objectMapper;

    public ResumeContentMapper(
            ResumeSectionRepository resumeSectionRepository,
            ObjectMapper objectMapper
    ) {
        this.resumeSectionRepository = resumeSectionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Convert ResumeVersion + ResumeSection entities into structured resume content.
     */
    public Map<String, Object> toStructuredContent(ResumeVersion version) {
        if (version == null) {
            throw new ResumeEditingException("Resume version cannot be null");
        }

        try {
            if (version.getStructuredContentJson() != null && !version.getStructuredContentJson().trim().isEmpty()) {
                return objectMapper.readValue(
                        version.getStructuredContentJson(),
                        new TypeReference<LinkedHashMap<String, Object>>() {}
                );
            }

            if (version.getResumeVersionId() == null) {
                return new LinkedHashMap<>();
            }

            List<ResumeSection> sections =
                    resumeSectionRepository.findByResumeVersion_ResumeVersionIdOrderBySectionOrderAsc(
                            version.getResumeVersionId()
                    );

            Map<String, Object> content = new LinkedHashMap<>();

            for (ResumeSection section : sections) {
                if (section == null || section.getSectionType() == null) {
                    continue;
                }

                String key = normalizeKey(section.getSectionType());

                if (section.getContentJson() != null && !section.getContentJson().trim().isEmpty()) {
                    Map<String, Object> value = objectMapper.readValue(
                            section.getContentJson(),
                            new TypeReference<LinkedHashMap<String, Object>>() {}
                    );
                    content.put(key, value);
                } else if (section.getPlainText() != null && !section.getPlainText().trim().isEmpty()) {
                    content.put(key, section.getPlainText().trim());
                }
            }

            return content;

        } catch (Exception ex) {
            throw new ResumeEditingException("Failed to map resume sections to structured content", ex);
        }
    }

    /**
     * Save structured resume content back into ResumeSection entities.
     */
    public void saveStructuredContent(ResumeVersion version, Map<String, Object> content) {
        if (version == null) {
            throw new ResumeEditingException("Resume version cannot be null");
        }

        Map<String, Object> safeContent = content == null ? new LinkedHashMap<>() : new LinkedHashMap<>(content);

        try {
            String json = objectMapper.writeValueAsString(safeContent);
            version.setStructuredContentJson(json);

            List<ResumeSection> existingSections = version.getResumeVersionId() == null
                    ? new ArrayList<>()
                    : resumeSectionRepository.findByResumeVersion_ResumeVersionIdOrderBySectionOrderAsc(
                            version.getResumeVersionId()
                    );

            Map<String, ResumeSection> existingMap = new HashMap<>();
            for (ResumeSection section : existingSections) {
                if (section.getSectionType() != null) {
                    existingMap.put(normalizeKey(section.getSectionType()), section);
                }
            }

            Set<Long> retainedSectionIds = new HashSet<>();
            int order = 1;

            for (Map.Entry<String, Object> entry : safeContent.entrySet()) {
                String key = normalizeKey(entry.getKey());
                if (key.isBlank()) {
                    continue;
                }

                Object value = entry.getValue();

                ResumeSection section = existingMap.getOrDefault(key, new ResumeSection());
                section.setResumeVersion(version);
                section.setSectionType(key.toUpperCase(Locale.ROOT));
                section.setSectionOrder(order++);
                section.setSectionTitle(toDisplayTitle(key));

                if (value instanceof String stringValue) {
                    section.setPlainText(trimToNull(stringValue));
                    section.setContentJson(null);
                } else {
                    section.setContentJson(objectMapper.writeValueAsString(value));
                    section.setPlainText(trimToNull(buildPlainText(value)));
                }

                ResumeSection saved = resumeSectionRepository.save(section);
                if (saved.getResumeSectionId() != null) {
                    retainedSectionIds.add(saved.getResumeSectionId());
                }
            }

            for (ResumeSection existing : existingSections) {
                if (existing.getResumeSectionId() != null && !retainedSectionIds.contains(existing.getResumeSectionId())) {
                    resumeSectionRepository.delete(existing);
                }
            }

        } catch (Exception ex) {
            throw new ResumeEditingException("Failed to save structured resume content", ex);
        }
    }

    /**
     * Merge updated content into existing resume content.
     */
    public Map<String, Object> mergeContent(
            Map<String, Object> existing,
            Map<String, Object> updates
    ) {
        Map<String, Object> safeExisting = existing == null ? new LinkedHashMap<>() : new LinkedHashMap<>(existing);

        if (updates == null || updates.isEmpty()) {
            return safeExisting;
        }

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = normalizeKey(entry.getKey());
            if (key.isBlank()) {
                continue;
            }

            Object newValue = entry.getValue();

            if (newValue == null) {
                safeExisting.remove(key);
                continue;
            }

            Object existingValue = safeExisting.get(key);

            if (existingValue instanceof Map<?, ?> && newValue instanceof Map<?, ?>) {
                safeExisting.put(key, mergeMaps((Map<?, ?>) existingValue, (Map<?, ?>) newValue));
            } else {
                safeExisting.put(key, newValue);
            }
        }

        return safeExisting;
    }

    /**
     * Convert ResumeVersion into a simple DTO-friendly structure.
     */
    public Map<String, Object> toEditorPayload(ResumeVersion version) {
        if (version == null) {
            throw new ResumeEditingException("Resume version cannot be null");
        }

        Map<String, Object> structured = toStructuredContent(version);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("resumeVersionId", version.getResumeVersionId());
        payload.put("versionCode", version.getVersionCode());
        payload.put("versionName", version.getVersionName());
        payload.put("versionType", version.getVersionType());
        payload.put("content", structured);
        payload.put("atsScore", version.getAtsScore());
        payload.put("fileUrl", version.getFileUrl());
        payload.put("previewUrl", version.getPreviewUrl());
        payload.put("profileSnapshotJson", trimToNull(version.getProfileSnapshotJson()));
        payload.put("formatMetadataJson", trimToNull(version.getFormatMetadataJson()));
        payload.put("createdAt", version.getCreatedAt());
        payload.put("updatedAt", version.getUpdatedAt());

        return payload;
    }

    /**
     * Convert a flat resume form input into structured JSON.
     */
    public Map<String, Object> fromFlatForm(Map<String, Object> formData) {
        Map<String, Object> structured = new LinkedHashMap<>();

        if (formData == null) {
            return structured;
        }

        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            String key = normalizeKey(entry.getKey());
            Object value = entry.getValue();

            if (key.isBlank() || value == null) {
                continue;
            }

            structured.put(key, value);
        }

        return structured;
    }

    /**
     * Normalize keys for consistent JSON structure.
     */
    private String normalizeKey(String key) {
        if (key == null) {
            return "";
        }

        return key.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    /**
     * Merge nested maps.
     */
    private Map<String, Object> mergeMaps(Map<?, ?> original, Map<?, ?> updates) {
        Map<String, Object> merged = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : original.entrySet()) {
            if (entry.getKey() != null) {
                merged.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }

        for (Map.Entry<?, ?> entry : updates.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }

            String key = String.valueOf(entry.getKey());
            Object newValue = entry.getValue();

            if (newValue == null) {
                merged.remove(key);
                continue;
            }

            Object existingValue = merged.get(key);

            if (existingValue instanceof Map<?, ?> && newValue instanceof Map<?, ?>) {
                merged.put(key, mergeMaps((Map<?, ?>) existingValue, (Map<?, ?>) newValue));
            } else {
                merged.put(key, newValue);
            }
        }

        return merged;
    }

    private String toDisplayTitle(String key) {
        String normalized = key == null ? "General" : key.trim().replace('_', ' ').toLowerCase(Locale.ROOT);
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}