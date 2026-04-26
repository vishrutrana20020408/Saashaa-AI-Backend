package backend.ai_interview.service.resume;

import backend.ai_interview.dto.response.ResumeProfileSnapshotResponse;
import backend.ai_interview.exception.ResumeParsingException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resume Parser Service
 *
 * Handles parsing and basic extraction of resume content from:
 * - PDF
 * - DOCX
 * - TXT
 *
 * Responsibilities:
 * - extract raw text from uploaded resume file
 * - identify common sections
 * - extract skills, email, phone, links
 * - produce a structured result usable by frontend/editor/service layer
 *
 * Latest project alignment:
 * - supports resume editor structured content flow
 * - supports parsed profile snapshot generation for profile sync
 * - supports format metadata extraction for preview/editor flows
 *
 * NOTE:
 * Legacy .doc is not supported by this implementation.
 * Supported formats are PDF, DOCX, and TXT only.
 */
@Service
@SuppressWarnings("all")
public class ResumeParserService {

    private static final Set<String> SKILL_KEYWORDS = new LinkedHashSet<>(Arrays.asList(
            "Java", "Spring Boot", "Spring", "Hibernate", "JPA", "MySQL", "PostgreSQL",
            "MongoDB", "Redis", "Kafka", "RabbitMQ", "Docker", "Kubernetes", "AWS",
            "Azure", "GCP", "Jenkins", "Git", "GitHub", "GitLab", "Maven", "Gradle",
            "Microservices", "REST", "REST API", "GraphQL", "JUnit", "Mockito",
            "React", "Next.js", "TypeScript", "JavaScript", "Node.js", "Express",
            "Python", "Django", "Flask", "C++", "C", "HTML", "CSS", "Tailwind",
            "Linux", "CI/CD", "Agile", "Scrum", "Data Structures", "Algorithms",
            "System Design", "Machine Learning", "TensorFlow", "PyTorch", "Pandas",
            "NumPy", "Power BI", "Tableau", "Excel", "Firebase", "Android", "Kotlin"
    ));

    private static final List<String> SECTION_ORDER = Arrays.asList(
            "summary",
            "objective",
            "skills",
            "technical skills",
            "experience",
            "work experience",
            "professional experience",
            "projects",
            "education",
            "certifications",
            "achievements",
            "publications"
    );

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d{1,3}[\\s-]?)?(\\d{10})");

    private static final Pattern LINK_PATTERN =
            Pattern.compile("(https?://\\S+|www\\.\\S+|linkedin\\.com/\\S+|github\\.com/\\S+)");

    /**
     * Parse a resume file and return a structured map.
     */
    public Map<String, Object> parse(MultipartFile file) {
        validateFile(file);

        try {
            String rawText = extractRawText(file);

            if (rawText.isBlank()) {
                throw new ResumeParsingException("Could not extract content from the resume file");
            }

            Map<String, String> sections = extractSections(rawText);
            List<String> links = extractLinks(rawText);
            List<String> skills = extractSkills(rawText);

            Map<String, Object> parsed = new LinkedHashMap<>();
            parsed.put("fileName", file.getOriginalFilename());
            parsed.put("contentType", file.getContentType());
            parsed.put("rawText", rawText);
            parsed.put("email", extractEmail(rawText));
            parsed.put("phone", extractPhone(rawText));
            parsed.put("links", links);
            parsed.put("name", extractName(rawText));
            parsed.put("skills", skills);
            parsed.put("sections", sections);
            parsed.put("score", estimateResumeScore(rawText));

            parsed.put("profileSnapshot", extractProfileSnapshotFromParsed(rawText, sections, links, skills));
            parsed.put("formatMetadata", extractFormatMetadataFromParsed(rawText, file, sections, links));

            return parsed;

        } catch (ResumeParsingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResumeParsingException("Failed to parse resume file", ex);
        }
    }

    /**
     * Extract only raw text from a resume file.
     */
    public String extractRawText(MultipartFile file) {
        validateFile(file);

        String fileName = safe(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        String contentType = safe(file.getContentType()).toLowerCase(Locale.ROOT);

        try {
            if (fileName.endsWith(".pdf") || contentType.contains("pdf")) {
                return parsePdf(file);
            }

            if (fileName.endsWith(".docx") || contentType.contains("wordprocessingml")) {
                return parseDocx(file);
            }

            if (fileName.endsWith(".txt") || contentType.contains("text/plain")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8).trim();
            }

            if (fileName.endsWith(".doc") || contentType.contains("msword")) {
                throw new ResumeParsingException("Legacy DOC files are not supported. Please upload PDF, DOCX, or TXT.");
            }

            throw new ResumeParsingException("Unsupported resume format. Please upload PDF, DOCX, or TXT.");

        } catch (ResumeParsingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResumeParsingException("Failed to extract text from resume", ex);
        }
    }

    /**
     * Extract basic structured content suitable for resume editor.
     */
    public Map<String, Object> extractStructuredContent(MultipartFile file) {
        String rawText = extractRawText(file);
        return extractStructuredContent(rawText);
    }

    /**
     * Extract structured content from raw text.
     */
    public Map<String, Object> extractStructuredContent(String rawText) {
        if (isBlank(rawText)) {
            throw new ResumeParsingException("Resume text is empty");
        }

        Map<String, String> sections = extractSections(rawText);
        List<String> links = extractLinks(rawText);
        List<String> skills = extractSkills(rawText);

        Map<String, Object> structured = new LinkedHashMap<>();
        structured.put("name", extractName(rawText));
        structured.put("email", extractEmail(rawText));
        structured.put("phone", extractPhone(rawText));
        structured.put("links", links);
        structured.put("summary", firstNonBlank(
                sections.get("summary"),
                sections.get("objective"),
                buildFallbackSummary(rawText)
        ));
        structured.put("skills", skills);
        structured.put("experience", normalizeParagraphSection(
                firstNonBlank(
                        sections.get("experience"),
                        sections.get("work experience"),
                        sections.get("professional experience")
                )
        ));
        structured.put("projects", normalizeParagraphSection(sections.get("projects")));
        structured.put("education", normalizeParagraphSection(sections.get("education")));
        structured.put("certifications", normalizeParagraphSection(sections.get("certifications")));
        structured.put("achievements", normalizeParagraphSection(sections.get("achievements")));
        structured.put("rawText", rawText);

        structured.put("profileSnapshot", extractProfileSnapshotFromParsed(rawText, sections, links, skills));
        structured.put("formatMetadata", extractFormatMetadataFromParsed(rawText, null, sections, links));

        return structured;
    }

    /**
     * Extract parsed profile snapshot from uploaded file.
     */
    public ResumeProfileSnapshotResponse extractProfileSnapshot(MultipartFile file) {
        String rawText = extractRawText(file);
        return extractProfileSnapshot(rawText);
    }

    /**
     * Extract parsed profile snapshot from raw text.
     */
    public ResumeProfileSnapshotResponse extractProfileSnapshot(String rawText) {
        if (isBlank(rawText)) {
            throw new ResumeParsingException("Resume text is empty");
        }

        Map<String, String> sections = extractSections(rawText);
        List<String> links = extractLinks(rawText);
        List<String> skills = extractSkills(rawText);

        return extractProfileSnapshotFromParsed(rawText, sections, links, skills);
    }

    /**
     * Extract resume format metadata from uploaded file.
     */
    public Map<String, Object> extractFormatMetadata(MultipartFile file) {
        String rawText = extractRawText(file);
        Map<String, String> sections = extractSections(rawText);
        List<String> links = extractLinks(rawText);
        return extractFormatMetadataFromParsed(rawText, file, sections, links);
    }

    /**
     * Extract resume format metadata from raw text and optional file.
     */
    public Map<String, Object> extractFormatMetadata(String rawText, MultipartFile file) {
        if (isBlank(rawText)) {
            throw new ResumeParsingException("Resume text is empty");
        }

        Map<String, String> sections = extractSections(rawText);
        List<String> links = extractLinks(rawText);
        return extractFormatMetadataFromParsed(rawText, file, sections, links);
    }

    /**
     * Estimate a simple ATS-like score from extracted content.
     */
    public int estimateResumeScore(String rawText) {
        if (isBlank(rawText)) {
            return 0;
        }

        int score = 30;
        Map<String, String> sections = extractSections(rawText);
        List<String> skills = extractSkills(rawText);

        if (!isBlank(extractEmail(rawText))) {
            score += 10;
        }
        if (!isBlank(extractPhone(rawText))) {
            score += 10;
        }
        if (!skills.isEmpty()) {
            score += Math.min(20, skills.size() * 2);
        }

        if (!isBlank(firstNonBlank(sections.get("summary"), sections.get("objective")))) {
            score += 10;
        }
        if (!isBlank(firstNonBlank(
                sections.get("experience"),
                sections.get("work experience"),
                sections.get("professional experience")
        ))) {
            score += 10;
        }
        if (!isBlank(sections.get("education"))) {
            score += 5;
        }
        if (!isBlank(sections.get("projects"))) {
            score += 5;
        }

        return Math.min(100, score);
    }

    private ResumeProfileSnapshotResponse extractProfileSnapshotFromParsed(
            String rawText,
            Map<String, String> sections,
            List<String> links,
            List<String> skills
    ) {
        ResumeProfileSnapshotResponse response =
                ResumeProfileSnapshotResponse.ok("Resume profile snapshot extracted successfully");

        response.setFullName(extractName(rawText));
        response.setEmail(extractEmail(rawText));
        response.setPhone(extractPhone(rawText));
        response.setLocation(extractLocation(rawText));
        response.setHeadline(extractHeadline(rawText));
        response.setProfileSummary(firstNonBlank(
                sections.get("summary"),
                sections.get("objective"),
                buildFallbackSummary(rawText)
        ));
        response.setLinkedinUrl(extractLinkedinUrl(links));
        response.setGithubUrl(extractGithubUrl(links));
        response.setPortfolioUrl(extractPortfolioUrl(links));
        response.setCurrentCompany(extractCurrentCompany(rawText, sections));
        response.setCurrentRole(extractCurrentRole(rawText, sections));
        response.setHighestEducation(extractHighestEducation(sections));
        response.setTopSkillsJson(toJsonArray(skills));
        response.setExperienceSummaryJson(toJsonArrayOfTitles(normalizeParagraphSection(
                firstNonBlank(
                        sections.get("experience"),
                        sections.get("work experience"),
                        sections.get("professional experience")
                )
        )));
        response.setEducationSummaryJson(toJsonArrayOfTitles(
                normalizeParagraphSection(sections.get("education"))
        ));

        return response;
    }

    private Map<String, Object> extractFormatMetadataFromParsed(
            String rawText,
            MultipartFile file,
            Map<String, String> sections,
            List<String> links
    ) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("fileName", file != null ? file.getOriginalFilename() : null);
        metadata.put("contentType", file != null ? file.getContentType() : null);
        metadata.put("layoutType", detectLayoutType(rawText));
        metadata.put("templateFamily", detectTemplateFamily(rawText, sections));
        metadata.put("sectionOrder", new ArrayList<>(sections.keySet()));
        metadata.put("hasProfileLinks", !links.isEmpty());
        metadata.put("hasIcons", Boolean.FALSE);
        metadata.put("headingStyle", detectHeadingStyle(rawText));
        metadata.put("bulletStyle", detectBulletStyle(rawText));
        metadata.put("fontHints", Collections.emptyList());
        metadata.put("colorUsage", "minimal");
        metadata.put("spacingDensity", detectSpacingDensity(rawText));
        metadata.put("pageCount", estimatePageCount(rawText));
        metadata.put("lineCount", normalizeText(rawText).isBlank() ? 0 : normalizeText(rawText).split("\n").length);
        return metadata;
    }

    private String parsePdf(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();
            return normalizeText(stripper.getText(document));

        } catch (Exception ex) {
            throw new ResumeParsingException("Failed to parse PDF resume", ex);
        }
    }

    private String parseDocx(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            return normalizeText(extractor.getText());

        } catch (Exception ex) {
            throw new ResumeParsingException("Failed to parse DOCX resume", ex);
        }
    }

    private Map<String, String> extractSections(String rawText) {
        List<String> lines = Arrays.stream(normalizeText(rawText).split("\n"))
                .map(String::trim)
                .collect(Collectors.toList());

        Map<String, StringBuilder> builders = new LinkedHashMap<>();
        String currentSection = "general";
        builders.put(currentSection, new StringBuilder());

        for (String line : lines) {
            if (line.isBlank()) {
                builders.putIfAbsent(currentSection, new StringBuilder());
                builders.get(currentSection).append("\n");
                continue;
            }

            String detectedSection = detectSectionHeader(line);
            if (detectedSection != null) {
                currentSection = detectedSection;
                builders.putIfAbsent(currentSection, new StringBuilder());
                continue;
            }

            builders.putIfAbsent(currentSection, new StringBuilder());
            builders.get(currentSection).append(line).append("\n");
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, StringBuilder> entry : builders.entrySet()) {
            String cleaned = entry.getValue().toString().trim();
            if (!cleaned.isBlank()) {
                result.put(entry.getKey(), cleaned);
            }
        }

        return result;
    }

    private String detectSectionHeader(String line) {
        String normalized = line.trim()
                .toLowerCase(Locale.ROOT)
                .replace(":", "")
                .replaceAll("\\s+", " ");

        for (String section : SECTION_ORDER) {
            if (normalized.equals(section)) {
                return section;
            }
        }

        return null;
    }

    private List<String> extractSkills(String rawText) {
        String normalized = " " + rawText.toLowerCase(Locale.ROOT) + " ";
        LinkedHashSet<String> matches = new LinkedHashSet<>();

        for (String skill : SKILL_KEYWORDS) {
            String pattern = "\\b" + Pattern.quote(skill.toLowerCase(Locale.ROOT)) + "\\b";
            if (Pattern.compile(pattern).matcher(normalized).find()) {
                matches.add(skill);
            }
        }

        return new ArrayList<>(matches);
    }

    private String extractEmail(String rawText) {
        Matcher matcher = EMAIL_PATTERN.matcher(rawText);
        return matcher.find() ? matcher.group().trim() : "";
    }

    private String extractPhone(String rawText) {
        Matcher matcher = PHONE_PATTERN.matcher(rawText.replaceAll("[()]", " "));
        return matcher.find() ? matcher.group().trim() : "";
    }

    private List<String> extractLinks(String rawText) {
        LinkedHashSet<String> links = new LinkedHashSet<>();
        Matcher matcher = LINK_PATTERN.matcher(rawText);

        while (matcher.find()) {
            links.add(matcher.group().trim());
        }

        return new ArrayList<>(links);
    }

    private String extractName(String rawText) {
        List<String> lines = Arrays.stream(normalizeText(rawText).split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(5)
                .collect(Collectors.toList());

        for (String line : lines) {
            if (line.length() > 2
                    && line.length() < 80
                    && !line.contains("@")
                    && !line.matches(".*\\d.*")
                    && Character.isUpperCase(line.charAt(0))) {
                return line;
            }
        }

        return "";
    }

    private String extractLocation(String rawText) {
        List<String> lines = Arrays.stream(normalizeText(rawText).split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(8)
                .collect(Collectors.toList());

        Pattern locationPattern = Pattern.compile("([A-Za-z ]+),\\s*([A-Za-z ]+)(,\\s*[A-Za-z ]+)?");

        for (String line : lines) {
            String lower = line.toLowerCase(Locale.ROOT);
            if (line.contains("@")
                    || line.matches(".*\\d{10}.*")
                    || lower.contains("linkedin")
                    || lower.contains("github")) {
                continue;
            }

            Matcher matcher = locationPattern.matcher(line);
            if (matcher.find() && line.length() <= 100) {
                return matcher.group().trim();
            }
        }

        return "";
    }

    private String extractHeadline(String rawText) {
        List<String> lines = Arrays.stream(normalizeText(rawText).split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(8)
                .collect(Collectors.toList());

        String detectedName = extractName(rawText);

        for (String line : lines) {
            if (line.equals(detectedName)) {
                continue;
            }
            if (line.contains("@") || line.matches(".*\\d.*")) {
                continue;
            }
            if (detectSectionHeader(line) != null) {
                continue;
            }
            if (line.length() >= 3 && line.length() <= 100) {
                return line;
            }
        }

        return "";
    }

    private String extractLinkedinUrl(List<String> links) {
        return links.stream()
                .filter(link -> link.toLowerCase(Locale.ROOT).contains("linkedin.com"))
                .findFirst()
                .orElse("");
    }

    private String extractGithubUrl(List<String> links) {
        return links.stream()
                .filter(link -> link.toLowerCase(Locale.ROOT).contains("github.com"))
                .findFirst()
                .orElse("");
    }

    private String extractPortfolioUrl(List<String> links) {
        return links.stream()
                .filter(link -> !link.toLowerCase(Locale.ROOT).contains("linkedin.com"))
                .filter(link -> !link.toLowerCase(Locale.ROOT).contains("github.com"))
                .findFirst()
                .orElse("");
    }

    private String extractCurrentCompany(String rawText, Map<String, String> sections) {
        String experience = firstNonBlank(
                sections.get("experience"),
                sections.get("work experience"),
                sections.get("professional experience")
        );

        if (isBlank(experience)) {
            return "";
        }

        List<String> lines = Arrays.stream(experience.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

        for (String line : lines) {
            if (line.matches(".*\\b(Inc|Ltd|LLC|Pvt|Technologies|Solutions|Systems|Company|Corp|Corporation)\\b.*")) {
                return line;
            }
        }

        if (lines.size() > 1) {
            return lines.get(1);
        }

        return "";
    }

    private String extractCurrentRole(String rawText, Map<String, String> sections) {
        String experience = firstNonBlank(
                sections.get("experience"),
                sections.get("work experience"),
                sections.get("professional experience")
        );

        if (isBlank(experience)) {
            return "";
        }

        List<String> lines = Arrays.stream(experience.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

        return lines.isEmpty() ? "" : lines.get(0);
    }

    private String extractHighestEducation(Map<String, String> sections) {
        String education = sections.get("education");

        if (isBlank(education)) {
            return "";
        }

        List<String> lines = Arrays.stream(education.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

        for (String line : lines) {
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.contains("b.tech")
                    || lower.contains("m.tech")
                    || lower.contains("bachelor")
                    || lower.contains("master")
                    || lower.contains("phd")
                    || lower.contains("mba")
                    || lower.contains("b.e")
                    || lower.contains("m.e")) {
                return line;
            }
        }

        return lines.isEmpty() ? "" : lines.get(0);
    }

    private List<Map<String, Object>> normalizeParagraphSection(String content) {
        if (isBlank(content)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        String[] blocks = content.split("\\n\\s*\\n");

        for (String block : blocks) {
            String trimmed = block.trim();
            if (trimmed.isBlank()) {
                continue;
            }

            List<String> lines = Arrays.stream(trimmed.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());

            if (lines.isEmpty()) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("title", lines.get(0));
            item.put("details", lines.size() > 1 ? lines.subList(1, lines.size()) : Collections.emptyList());
            result.add(item);
        }

        return result;
    }

    private String buildFallbackSummary(String rawText) {
        List<String> lines = Arrays.stream(normalizeText(rawText).split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

        StringBuilder summary = new StringBuilder();
        int count = 0;

        for (String line : lines) {
            if (line.contains("@") || line.matches(".*\\d{10}.*")) {
                continue;
            }
            if (detectSectionHeader(line) != null) {
                break;
            }

            if (summary.length() > 0) {
                summary.append(" ");
            }
            summary.append(line);
            count++;

            if (count >= 3) {
                break;
            }
        }

        return summary.toString().trim();
    }

    private String detectLayoutType(String rawText) {
        long shortLines = Arrays.stream(normalizeText(rawText).split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> line.length() < 35)
                .count();

        long totalLines = Arrays.stream(normalizeText(rawText).split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .count();

        if (totalLines > 0 && shortLines > (totalLines / 3)) {
            return "two_column_like";
        }

        return "single_column";
    }

    private String detectTemplateFamily(String rawText, Map<String, String> sections) {
        boolean hasSummary = !isBlank(firstNonBlank(
                sections.get("summary"),
                sections.get("objective")
        ));
        boolean hasProjects = !isBlank(sections.get("projects"));
        boolean hasSkills = !extractSkills(rawText).isEmpty();

        if (hasSummary && hasProjects && hasSkills) {
            return "modern_minimal";
        }

        if (hasSkills && hasProjects) {
            return "technical_standard";
        }

        return "classic";
    }

    private String detectHeadingStyle(String rawText) {
        List<String> lines = Arrays.stream(normalizeText(rawText).split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());

        for (String line : lines) {
            String section = detectSectionHeader(line);
            if (section != null) {
                if (line.equals(line.toUpperCase(Locale.ROOT))) {
                    return "uppercase";
                }
                return "title_case";
            }
        }

        return "standard";
    }

    private String detectBulletStyle(String rawText) {
        if (rawText.contains("•")) {
            return "dot";
        }
        if (rawText.contains("-")) {
            return "dash";
        }
        return "plain";
    }

    private String detectSpacingDensity(String rawText) {
        String normalized = normalizeText(rawText);
        int lineCount = normalized.isBlank() ? 0 : normalized.split("\n").length;
        int charCount = normalized.length();

        if (lineCount == 0) {
            return "normal";
        }

        double avg = (double) charCount / lineCount;

        if (avg > 80) {
            return "compact";
        }
        if (avg < 35) {
            return "spacious";
        }
        return "normal";
    }

    private int estimatePageCount(String rawText) {
        int length = normalizeText(rawText).length();
        if (length <= 2500) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil(length / 2500.0));
    }

    private String toJsonArray(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .map(v -> "\"" + escapeJson(v) + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String toJsonArrayOfTitles(List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }

        List<String> titles = new ArrayList<>();
        for (Map<String, Object> item : items) {
            if (item == null) {
                continue;
            }
            Object title = item.get("title");
            if (title != null && !title.toString().trim().isBlank()) {
                titles.add(title.toString().trim());
            }
        }

        return toJsonArray(titles);
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private void validateFile(MultipartFile file) {
        if (file == null) {
            throw new ResumeParsingException("Resume file is required");
        }
        if (file.isEmpty()) {
            throw new ResumeParsingException("Resume file is empty");
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\t", " ")
                .replace('\u00A0', ' ')
                .replaceAll("[ ]{2,}", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}