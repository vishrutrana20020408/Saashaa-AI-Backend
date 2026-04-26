package backend.ai_interview.service;

import backend.ai_interview.entity.ResumeSection;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.exception.FileGenerationException;
import backend.ai_interview.repository.ResumeSectionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * File Generation Service
 *
 * Handles generation of resume-related output files/content.
 *
 * Current starter capabilities:
 * - generate HTML preview content from ResumeVersion
 * - generate plain text export from ResumeVersion
 * - generate byte[] payloads for HTML/TXT export
 * - build safe file names for generated resume assets
 *
 * NOTE:
 * This starter implementation does not generate real PDF/DOCX binaries yet.
 * It provides clean HTML/TXT generation that can later be extended with:
 * - PDF renderer
 * - DOCX generator
 * - template engine based rendering
 *
 * Latest project update:
 * - keeps generated output aligned with resume preview/editor flow
 * - includes parsed profile snapshot content when available
 * - includes resume format metadata when available
 * - generates stable file names for version-based resume assets
 */
@Service
@SuppressWarnings("all")
public class FileGenerationService {

    private final ResumeSectionRepository resumeSectionRepository;
    private final ObjectMapper objectMapper;

    public FileGenerationService(
            ResumeSectionRepository resumeSectionRepository,
            ObjectMapper objectMapper
    ) {
        this.resumeSectionRepository = resumeSectionRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate HTML preview content for a resume version.
     */
    public String generateHtml(ResumeVersion version) {
        if (version == null) {
            throw new FileGenerationException("Resume version is required");
        }

        try {
            Map<String, Object> structuredContent = readStructuredContent(version);
            Map<String, Object> profileSnapshot = readJsonMap(version.getProfileSnapshotJson());
            Map<String, Object> formatMetadata = readJsonMap(version.getFormatMetadataJson());

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>");
            html.append("<html lang=\"en\">");
            html.append("<head>");
            html.append("<meta charset=\"UTF-8\"/>");
            html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>");
            html.append("<title>").append(escapeHtml(version.getVersionName())).append("</title>");
            html.append("<style>");
            html.append(buildHtmlStyles());
            html.append("</style>");
            html.append("</head>");
            html.append("<body>");
            html.append("<div class=\"resume-container\">");

            renderHeader(html, structuredContent, version);
            renderVersionMeta(html, version);
            renderProfileSnapshot(html, profileSnapshot);
            renderFormatMetadata(html, formatMetadata);
            renderSections(html, structuredContent);

            html.append("</div>");
            html.append("</body>");
            html.append("</html>");

            return html.toString();

        } catch (FileGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FileGenerationException("Failed to generate HTML resume", ex);
        }
    }

    /**
     * Generate HTML bytes for storage/export.
     */
    public byte[] generateHtmlBytes(ResumeVersion version) {
        return generateHtml(version).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generate plain text export for a resume version.
     */
    public String generatePlainText(ResumeVersion version) {
        if (version == null) {
            throw new FileGenerationException("Resume version is required");
        }

        try {
            if (version.getRawText() != null && !version.getRawText().trim().isEmpty()) {
                return version.getRawText().trim();
            }

            Map<String, Object> structuredContent = readStructuredContent(version);
            Map<String, Object> profileSnapshot = readJsonMap(version.getProfileSnapshotJson());
            Map<String, Object> formatMetadata = readJsonMap(version.getFormatMetadataJson());

            StringBuilder text = new StringBuilder();

            String name = valueAsString(structuredContent.get("name"));
            if (!name.isBlank()) {
                text.append(name).append("\n");
            }

            List<String> contactParts = new ArrayList<>();
            String email = valueAsString(structuredContent.get("email"));
            String phone = valueAsString(structuredContent.get("phone"));

            if (!email.isBlank()) {
                contactParts.add(email);
            }
            if (!phone.isBlank()) {
                contactParts.add(phone);
            }

            Object linksObject = structuredContent.get("links");
            if (linksObject instanceof List<?> links && !links.isEmpty()) {
                for (Object link : links) {
                    String value = valueAsString(link);
                    if (!value.isBlank()) {
                        contactParts.add(value);
                    }
                }
            }

            if (!contactParts.isEmpty()) {
                text.append(String.join(" | ", contactParts)).append("\n");
            }

            if (version.getVersionName() != null && !version.getVersionName().trim().isEmpty()) {
                text.append("Version: ").append(version.getVersionName().trim()).append("\n");
            }
            if (version.getVersionType() != null && !version.getVersionType().trim().isEmpty()) {
                text.append("Version Type: ").append(version.getVersionType().trim()).append("\n");
            }
            if (version.getAtsScore() != null) {
                text.append("ATS Score: ").append(version.getAtsScore()).append("\n");
            }

            if (text.length() > 0) {
                text.append("\n");
            }

            if (!profileSnapshot.isEmpty()) {
                text.append("PARSED PROFILE SNAPSHOT").append("\n");
                text.append(renderPlainTextValue(profileSnapshot)).append("\n\n");
            }

            if (!formatMetadata.isEmpty()) {
                text.append("FORMAT METADATA").append("\n");
                text.append(renderPlainTextValue(formatMetadata)).append("\n\n");
            }

            for (Map.Entry<String, Object> entry : structuredContent.entrySet()) {
                String key = entry.getKey();
                if (isHeaderField(key)) {
                    continue;
                }

                String sectionTitle = toDisplayTitle(key);
                String sectionText = renderPlainTextValue(entry.getValue());

                if (sectionText.isBlank()) {
                    continue;
                }

                text.append(sectionTitle.toUpperCase(Locale.ROOT)).append("\n");
                text.append(sectionText).append("\n\n");
            }

            return text.toString().trim();

        } catch (FileGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FileGenerationException("Failed to generate plain text resume", ex);
        }
    }

    /**
     * Generate plain text bytes for storage/export.
     */
    public byte[] generatePlainTextBytes(ResumeVersion version) {
        return generatePlainText(version).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generate a safe HTML file name.
     */
    public String buildHtmlFileName(ResumeVersion version) {
        return buildBaseFileName(version) + ".html";
    }

    /**
     * Generate a safe TXT file name.
     */
    public String buildTextFileName(ResumeVersion version) {
        return buildBaseFileName(version) + ".txt";
    }

    /**
     * Generate a safe PDF file name placeholder.
     */
    public String buildPdfFileName(ResumeVersion version) {
        return buildBaseFileName(version) + ".pdf";
    }

    /**
     * Generate a safe DOCX file name placeholder.
     */
    public String buildDocxFileName(ResumeVersion version) {
        return buildBaseFileName(version) + ".docx";
    }

    private Map<String, Object> readStructuredContent(ResumeVersion version) {
        if (version.getStructuredContentJson() != null && !version.getStructuredContentJson().trim().isEmpty()) {
            try {
                return objectMapper.readValue(
                        version.getStructuredContentJson(),
                        new TypeReference<LinkedHashMap<String, Object>>() {}
                );
            } catch (Exception ex) {
                throw new FileGenerationException("Failed to parse structured resume content", ex);
            }
        }

        if (version.getResumeVersionId() == null) {
            return new LinkedHashMap<>();
        }

        List<ResumeSection> sections =
                resumeSectionRepository.findByResumeVersion_ResumeVersionIdOrderBySectionOrderAsc(
                        version.getResumeVersionId()
                );

        Map<String, Object> structuredContent = new LinkedHashMap<>();

        for (ResumeSection section : sections) {
            if (section == null || section.getSectionType() == null || section.getSectionType().trim().isEmpty()) {
                continue;
            }

            String key = toStructuredContentKey(section.getSectionType());

            if (section.getContentJson() != null && !section.getContentJson().trim().isEmpty()) {
                try {
                    Map<String, Object> value = objectMapper.readValue(
                            section.getContentJson(),
                            new TypeReference<LinkedHashMap<String, Object>>() {}
                    );
                    structuredContent.put(key, value);
                    continue;
                } catch (Exception ignored) {
                    // fall through to plain text
                }
            }

            if (section.getPlainText() != null && !section.getPlainText().trim().isEmpty()) {
                structuredContent.put(key, section.getPlainText().trim());
            }
        }

        return structuredContent;
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.trim().isEmpty()) {
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

    private void renderHeader(StringBuilder html, Map<String, Object> structuredContent, ResumeVersion version) {
        html.append("<header class=\"resume-header\">");

        String name = valueAsString(structuredContent.get("name"));
        if (name.isBlank()) {
            name = valueAsString(version.getVersionName());
        }

        html.append("<h1>").append(escapeHtml(name)).append("</h1>");

        List<String> contactParts = new ArrayList<>();

        String email = valueAsString(structuredContent.get("email"));
        String phone = valueAsString(structuredContent.get("phone"));

        if (!email.isBlank()) {
            contactParts.add(email);
        }
        if (!phone.isBlank()) {
            contactParts.add(phone);
        }

        Object linksObject = structuredContent.get("links");
        if (linksObject instanceof List<?> links) {
            for (Object link : links) {
                String value = valueAsString(link);
                if (!value.isBlank()) {
                    contactParts.add(value);
                }
            }
        }

        if (!contactParts.isEmpty()) {
            html.append("<p class=\"contact\">")
                    .append(escapeHtml(String.join(" | ", contactParts)))
                    .append("</p>");
        }

        html.append("</header>");
    }

    private void renderVersionMeta(StringBuilder html, ResumeVersion version) {
        html.append("<section class=\"resume-section resume-meta\">");
        html.append("<h2>Version Details</h2>");
        html.append("<div class=\"resume-block\">");

        appendMetaRow(html, "Version Name", version.getVersionName());
        appendMetaRow(html, "Version Code", version.getVersionCode());
        appendMetaRow(html, "Version Type", version.getVersionType());
        appendMetaRow(html, "Job Application Code", version.getJobApplicationCode());

        if (version.getAtsScore() != null) {
            appendMetaRow(html, "ATS Score", String.valueOf(version.getAtsScore()));
        }

        appendMetaRow(html, "File URL", version.getFileUrl());
        appendMetaRow(html, "Preview URL", version.getPreviewUrl());

        html.append("</div>");
        html.append("</section>");
    }

    private void renderProfileSnapshot(StringBuilder html, Map<String, Object> profileSnapshot) {
        if (profileSnapshot == null || profileSnapshot.isEmpty()) {
            return;
        }

        html.append("<section class=\"resume-section\">");
        html.append("<h2>Parsed Profile Snapshot</h2>");
        html.append(renderHtmlValue(profileSnapshot));
        html.append("</section>");
    }

    private void renderFormatMetadata(StringBuilder html, Map<String, Object> formatMetadata) {
        if (formatMetadata == null || formatMetadata.isEmpty()) {
            return;
        }

        html.append("<section class=\"resume-section\">");
        html.append("<h2>Format Metadata</h2>");
        html.append(renderHtmlValue(formatMetadata));
        html.append("</section>");
    }

    private void renderSections(StringBuilder html, Map<String, Object> structuredContent) {
        for (Map.Entry<String, Object> entry : structuredContent.entrySet()) {
            String key = entry.getKey();
            if (isHeaderField(key)) {
                continue;
            }

            Object value = entry.getValue();
            if (isEmptyValue(value)) {
                continue;
            }

            html.append("<section class=\"resume-section\">");
            html.append("<h2>").append(escapeHtml(toDisplayTitle(key))).append("</h2>");
            html.append(renderHtmlValue(value));
            html.append("</section>");
        }
    }

    private void appendMetaRow(StringBuilder html, String label, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        html.append("<div class=\"resume-row\">");
        html.append("<strong>")
                .append(escapeHtml(label))
                .append(":</strong> ")
                .append(escapeHtml(value.trim()));
        html.append("</div>");
    }

    private String renderHtmlValue(Object value) {
        if (value == null) {
            return "";
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
                html.append("<li>").append(renderInlineHtmlValue(item)).append("</li>");
            }
            html.append("</ul>");
            return html.toString();
        }

        if (value instanceof Map<?, ?> mapValue) {
            StringBuilder html = new StringBuilder();
            html.append("<div class=\"resume-block\">");
            for (Map.Entry<?, ?> mapEntry : mapValue.entrySet()) {
                String key = mapEntry.getKey() == null ? "" : String.valueOf(mapEntry.getKey()).trim();
                Object mapEntryValue = mapEntry.getValue();

                if (isEmptyValue(mapEntryValue)) {
                    continue;
                }

                html.append("<div class=\"resume-row\">");
                if (!key.isBlank()) {
                    html.append("<strong>")
                            .append(escapeHtml(toDisplayTitle(key)))
                            .append(":</strong> ");
                }
                html.append(renderInlineHtmlValue(mapEntryValue));
                html.append("</div>");
            }
            html.append("</div>");
            return html.toString();
        }

        return "<p>" + escapeHtml(String.valueOf(value)) + "</p>";
    }

    private String renderInlineHtmlValue(Object value) {
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
                html.append("<li>").append(renderInlineHtmlValue(item)).append("</li>");
            }
            html.append("</ul>");
            return html.toString();
        }

        if (value instanceof Map<?, ?> mapValue) {
            StringBuilder html = new StringBuilder();
            boolean first = true;
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                if (!first) {
                    html.append("<br/>");
                }
                first = false;

                String key = entry.getKey() == null ? "" : String.valueOf(entry.getKey()).trim();
                if (!key.isBlank()) {
                    html.append("<strong>")
                            .append(escapeHtml(toDisplayTitle(key)))
                            .append(":</strong> ");
                }

                html.append(renderInlineHtmlValue(entry.getValue()));
            }
            return html.toString();
        }

        return escapeHtml(String.valueOf(value));
    }

    private String renderPlainTextValue(Object value) {
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
            StringBuilder text = new StringBuilder();
            for (Object item : listValue) {
                String rendered = renderPlainTextValue(item);
                if (!rendered.isBlank()) {
                    if (text.length() > 0) {
                        text.append("\n");
                    }
                    text.append("- ").append(rendered);
                }
            }
            return text.toString().trim();
        }

        if (value instanceof Map<?, ?> mapValue) {
            StringBuilder text = new StringBuilder();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                String key = entry.getKey() == null ? "" : String.valueOf(entry.getKey()).trim();
                String rendered = renderPlainTextValue(entry.getValue());

                if (rendered.isBlank()) {
                    continue;
                }

                if (text.length() > 0) {
                    text.append("\n");
                }

                if (!key.isBlank()) {
                    text.append(toDisplayTitle(key)).append(": ").append(rendered);
                } else {
                    text.append(rendered);
                }
            }
            return text.toString().trim();
        }

        return String.valueOf(value).trim();
    }

    private String buildBaseFileName(ResumeVersion version) {
        String versionName = version == null ? "resume-version" : valueAsString(version.getVersionName());
        if (versionName.isBlank()) {
            versionName = "resume-version";
        }

        String normalized = versionName
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (normalized.isBlank()) {
            normalized = "resume-version";
        }

        String code = version != null && version.getVersionCode() != null && !version.getVersionCode().trim().isEmpty()
                ? version.getVersionCode().trim()
                : "generated";

        String safeCode = code.replaceAll("[^A-Za-z0-9_-]+", "");

        return normalized + "-" + safeCode;
    }

    private boolean isHeaderField(String key) {
        if (key == null) {
            return false;
        }

        String normalized = key.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("name")
                || normalized.equals("email")
                || normalized.equals("phone")
                || normalized.equals("links")
                || normalized.equals("rawtext")
                || normalized.equals("raw_text")
                || normalized.equals("profilesnapshot")
                || normalized.equals("profile_snapshot")
                || normalized.equals("formatmetadata")
                || normalized.equals("format_metadata");
    }

    private boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String stringValue) {
            return stringValue.trim().isEmpty();
        }

        if (value instanceof Collection<?> collectionValue) {
            return collectionValue.isEmpty();
        }

        if (value instanceof Map<?, ?> mapValue) {
            return mapValue.isEmpty();
        }

        return false;
    }

    private String toStructuredContentKey(String sectionType) {
        return sectionType == null
                ? "general"
                : sectionType.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private String toDisplayTitle(String value) {
        String normalized = value == null ? "General" : value.trim().replace('_', ' ').toLowerCase(Locale.ROOT);
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

    private String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
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

    private String buildHtmlStyles() {
        return """
                body {
                    font-family: Arial, sans-serif;
                    background: #ffffff;
                    color: #111111;
                    margin: 0;
                    padding: 24px;
                    line-height: 1.5;
                }
                .resume-container {
                    max-width: 900px;
                    margin: 0 auto;
                }
                .resume-header {
                    border-bottom: 2px solid #222;
                    padding-bottom: 16px;
                    margin-bottom: 24px;
                }
                .resume-header h1 {
                    margin: 0 0 8px 0;
                    font-size: 30px;
                }
                .resume-header .contact {
                    margin: 0;
                    font-size: 14px;
                }
                .resume-section {
                    margin-bottom: 24px;
                }
                .resume-section h2 {
                    font-size: 18px;
                    margin: 0 0 10px 0;
                    padding-bottom: 6px;
                    border-bottom: 1px solid #ccc;
                    text-transform: uppercase;
                }
                .resume-block {
                    display: block;
                }
                .resume-row {
                    margin-bottom: 8px;
                }
                ul {
                    margin: 0;
                    padding-left: 20px;
                }
                p {
                    margin: 0 0 10px 0;
                }
                pre {
                    white-space: pre-wrap;
                    word-wrap: break-word;
                }
                """;
    }
}