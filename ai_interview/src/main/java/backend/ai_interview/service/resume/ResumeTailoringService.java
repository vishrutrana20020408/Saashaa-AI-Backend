package backend.ai_interview.service.resume;

import backend.ai_interview.dto.request.ResumeTailorRequest;
import backend.ai_interview.dto.request.ToolKnowledgeAnswerRequest;
import backend.ai_interview.dto.response.ResumeTailorResponse;
import backend.ai_interview.exception.ResumeTailoringException;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.entity.ResumeSection;
import backend.ai_interview.repository.ResumeVersionRepository;
import backend.ai_interview.repository.ResumeSectionRepository;
import backend.ai_interview.service.integration.ai.AiEngineClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Resume Tailoring Service
 *
 * Handles:
 * - extracting tools/skills/keywords from a job description
 * - tailoring a resume duplicate for ATS optimization
 * - processing user answers for required tools
 *
 * NOTE:
 * This service now uses AI-Engine for intelligent tailoring and tool extraction.
 * Falls back to rule-based implementation if AI-Engine is unavailable.
 */
@Service
@SuppressWarnings("all")
public class ResumeTailoringService {

    private final AiEngineClient aiEngineClient;
    private final ResumeVersionRepository resumeVersionRepository;
    private final ResumeSectionRepository resumeSectionRepository;

    public ResumeTailoringService(
            AiEngineClient aiEngineClient,
            ResumeVersionRepository resumeVersionRepository,
            ResumeSectionRepository resumeSectionRepository
    ) {
        this.aiEngineClient = aiEngineClient;
        this.resumeVersionRepository = resumeVersionRepository;
        this.resumeSectionRepository = resumeSectionRepository;
    }

    private static final Set<String> COMMON_TOOLS = new LinkedHashSet<>(Arrays.asList(
            "Java", "Spring Boot", "Spring", "Hibernate", "JPA", "MySQL", "PostgreSQL",
            "MongoDB", "Redis", "Kafka", "RabbitMQ", "Docker", "Kubernetes", "AWS",
            "Azure", "GCP", "Jenkins", "Git", "GitHub", "GitLab", "Maven", "Gradle",
            "Microservices", "REST API", "REST", "GraphQL", "JUnit", "Mockito",
            "React", "Next.js", "TypeScript", "JavaScript", "Node.js", "Express",
            "Python", "Django", "Flask", "C++", "C", "HTML", "CSS", "Tailwind",
            "Linux", "CI/CD", "Agile", "Scrum", "Data Structures", "Algorithms",
            "System Design", "Machine Learning", "TensorFlow", "PyTorch", "Pandas",
            "NumPy", "Power BI", "Tableau", "Excel", "Firebase", "Android", "Kotlin"
    ));

    /**
     * Extract tools, keywords and recommended skills from job description.
     * Uses AI-Engine for intelligent extraction, falls back to rule-based.
     */
    @SuppressWarnings("unchecked")
    public ResumeTailorResponse extractTools(String userId, ResumeTailorRequest request) {
        validateUser(userId);
        validateTailorRequest(request);

        try {
            String jobDescription = safe(request.getJobDescription());

            // Try AI-Engine extraction first
            try {
                Map<String, Object> payload = aiEngineClient.payloadOf("job_description", jobDescription);
                Map<String, Object> aiResponse = aiEngineClient.extractResumeTools(payload);

                // Extract tools from AI response
                List<String> extractedTools = new ArrayList<>();
                Object toolsObj = aiResponse.get("tools");
                if (toolsObj instanceof List) {
                    extractedTools.addAll((List<String>) toolsObj);
                }
                Object frameworksObj = aiResponse.get("frameworks");
                if (frameworksObj instanceof List) {
                    extractedTools.addAll((List<String>) frameworksObj);
                }
                Object languagesObj = aiResponse.get("languages");
                if (languagesObj instanceof List) {
                    extractedTools.addAll((List<String>) languagesObj);
                }
                Object platformsObj = aiResponse.get("platforms");
                if (platformsObj instanceof List) {
                    extractedTools.addAll((List<String>) platformsObj);
                }

                List<String> extractedKeywords = new ArrayList<>();
                Object keywordsObj = aiResponse.get("normalized_keywords");
                if (keywordsObj instanceof List) {
                    extractedKeywords.addAll((List<String>) keywordsObj);
                }

                List<String> knownTools = normalizeList(request.getKnownTools());
                List<String> unknownTools = normalizeList(request.getUnknownTools());
                List<String> recommendedSkills = buildRecommendedSkills(extractedTools, extractedKeywords);

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("companyName", trimToNull(request.getCompanyName()));
                result.put("jobTitle", trimToNull(request.getJobTitle()));
                result.put("resumeVersionId", request.getResumeVersionId());
                result.put("extractedTools", extractedTools);
                result.put("keywords", extractedKeywords);
                result.put("recommendedSkills", recommendedSkills);
                result.put("knownTools", knownTools);
                result.put("unknownTools", unknownTools);
                result.put("generatePreview", request.shouldGeneratePreview());
                result.put("additionalNotes", trimToNull(request.getAdditionalNotes()));

                ResumeTailorResponse response = ResumeTailorResponse.success("Tools extracted successfully using AI");
                response.setCompanyName(trimToNull(request.getCompanyName()));
                response.setJobTitle(trimToNull(request.getJobTitle()));
                response.setResumeVersionId(request.getResumeVersionId());
                response.setDetectedTools(extractedTools);
                response.setKeywords(extractedKeywords);
                response.setRecommendedSkills(recommendedSkills);
                response.setTailoredContent(result);
                response.setAtsScoreBefore(calculateBaseAtsScore(jobDescription));
                response.setAtsScoreAfter(calculateImprovedAtsScore(jobDescription, extractedTools, knownTools));
                response.setPreviewGenerated(request.shouldGeneratePreview());
                return response;

            } catch (Exception e) {
                System.err.println("AI-Engine tool extraction failed, falling back to local: " + e.getMessage());
            }

            // Fallback to local extraction
            return extractToolsLocal(userId, request);

        } catch (ResumeTailoringException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResumeTailoringException("Failed to extract tools from job description", ex);
        }
    }

    /**
     * Local rule-based tool extraction (fallback implementation).
     */
    private ResumeTailorResponse extractToolsLocal(String userId, ResumeTailorRequest request) {
        String jobDescription = safe(request.getJobDescription());

        List<String> extractedTools = extractKnownTools(jobDescription);
        List<String> extractedKeywords = extractKeywords(jobDescription);
        List<String> knownTools = normalizeList(request.getKnownTools());
        List<String> unknownTools = normalizeList(request.getUnknownTools());
        List<String> recommendedSkills = buildRecommendedSkills(extractedTools, extractedKeywords);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("companyName", trimToNull(request.getCompanyName()));
        result.put("jobTitle", trimToNull(request.getJobTitle()));
        result.put("resumeVersionId", request.getResumeVersionId());
        result.put("extractedTools", extractedTools);
        result.put("keywords", extractedKeywords);
        result.put("recommendedSkills", recommendedSkills);
        result.put("knownTools", knownTools);
        result.put("unknownTools", unknownTools);
        result.put("generatePreview", request.shouldGeneratePreview());
        result.put("additionalNotes", trimToNull(request.getAdditionalNotes()));

        ResumeTailorResponse response = ResumeTailorResponse.success("Tools extracted successfully (local)");
        response.setCompanyName(trimToNull(request.getCompanyName()));
        response.setJobTitle(trimToNull(request.getJobTitle()));
        response.setResumeVersionId(request.getResumeVersionId());
        response.setDetectedTools(extractedTools);
        response.setKeywords(extractedKeywords);
        response.setRecommendedSkills(recommendedSkills);
        response.setTailoredContent(result);
        response.setAtsScoreBefore(calculateBaseAtsScore(jobDescription));
        response.setAtsScoreAfter(calculateImprovedAtsScore(jobDescription, extractedTools, knownTools));
        response.setPreviewGenerated(request.shouldGeneratePreview());
        return response;
    }

    /**
     * Tailor resume content for a target job.
     * Uses AI-Engine for intelligent tailoring, falls back to rule-based.
     */
    @SuppressWarnings("unchecked")
    public ResumeTailorResponse tailorResume(String userId, ResumeTailorRequest request) {
        validateUser(userId);
        validateTailorRequest(request);

        try {
            String resumeText = extractResumeText(request.getResumeVersionId());
            String jobDescription = safe(request.getJobDescription());
            String companyName = trimToNull(request.getCompanyName());
            String jobTitle = trimToNull(request.getJobTitle());

            // Try AI-Engine tailoring first
            try {
                Map<String, Object> payload = aiEngineClient.payloadOf(
                    "resume_text", resumeText,
                    "job_description", jobDescription,
                    "job_title", jobTitle != null ? jobTitle : "",
                    "company_name", companyName != null ? companyName : "",
                    "known_tools", request.getKnownTools() != null ? request.getKnownTools() : new ArrayList<>()
                );
                Map<String, Object> aiResponse = aiEngineClient.tailorResume(payload);

                // Extract tailored content from AI response
                Object tailoredTextObj = aiResponse.get("tailored_resume_text");
                String tailoredText = tailoredTextObj instanceof String ? (String) tailoredTextObj : resumeText;

                List<String> matchedKeywords = new ArrayList<>();
                Object matchedObj = aiResponse.get("matched_keywords");
                if (matchedObj instanceof List) {
                    matchedKeywords.addAll((List<String>) matchedObj);
                }

                List<String> missingKeywords = new ArrayList<>();
                Object missingObj = aiResponse.get("missing_keywords");
                if (missingObj instanceof List) {
                    missingKeywords.addAll((List<String>) missingObj);
                }

                List<String> appliedChanges = new ArrayList<>();
                Object changesObj = aiResponse.get("applied_changes");
                if (changesObj instanceof List) {
                    appliedChanges.addAll((List<String>) changesObj);
                }

                List<String> suggestions = new ArrayList<>();
                Object suggestionsObj = aiResponse.get("suggestions");
                if (suggestionsObj instanceof List) {
                    suggestions.addAll((List<String>) suggestionsObj);
                }

                Integer atsScoreBefore = null;
                Object beforeObj = aiResponse.get("ats_score_before");
                if (beforeObj instanceof Number) {
                    atsScoreBefore = ((Number) beforeObj).intValue();
                }

                Integer atsScoreAfter = null;
                Object afterObj = aiResponse.get("ats_score_after");
                if (afterObj instanceof Number) {
                    atsScoreAfter = ((Number) afterObj).intValue();
                }

                Map<String, Object> tailoredContent = new LinkedHashMap<>();
                tailoredContent.put("companyName", companyName);
                tailoredContent.put("jobTitle", jobTitle);
                tailoredContent.put("resumeVersionId", request.getResumeVersionId());
                tailoredContent.put("tailoredResumeText", tailoredText);
                tailoredContent.put("matchedKeywords", matchedKeywords);
                tailoredContent.put("missingKeywords", missingKeywords);
                tailoredContent.put("appliedChanges", appliedChanges);
                tailoredContent.put("suggestions", suggestions);
                tailoredContent.put("generatePreview", request.shouldGeneratePreview());
                tailoredContent.put("knownTools", request.getKnownTools());
                tailoredContent.put("unknownTools", request.getUnknownTools());
                tailoredContent.put("notes", trimToNull(request.getAdditionalNotes()));

                ResumeTailorResponse response = ResumeTailorResponse.success("Resume tailored successfully using AI");
                response.setCompanyName(companyName);
                response.setJobTitle(jobTitle);
                response.setResumeVersionId(request.getResumeVersionId());
                response.setDetectedTools(matchedKeywords); // Using matched keywords as detected tools
                response.setKeywords(matchedKeywords);
                response.setRecommendedSkills(suggestions);
                response.setTailoredContent(tailoredContent);
                response.setAtsScoreBefore(atsScoreBefore != null ? atsScoreBefore : calculateBaseAtsScore(jobDescription));
                response.setAtsScoreAfter(atsScoreAfter != null ? atsScoreAfter : calculateImprovedAtsScore(jobDescription, matchedKeywords, request.getKnownTools() != null ? request.getKnownTools() : new ArrayList<>()));
                response.setPreviewGenerated(request.shouldGeneratePreview());
                return response;

            } catch (Exception e) {
                System.err.println("AI-Engine resume tailoring failed, falling back to local: " + e.getMessage());
            }

            // Fallback to local tailoring
            return tailorResumeLocal(userId, request);

        } catch (ResumeTailoringException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResumeTailoringException("Failed to tailor resume", ex);
        }
    }

    /**
     * Local rule-based resume tailoring (fallback implementation).
     */
    private ResumeTailorResponse tailorResumeLocal(String userId, ResumeTailorRequest request) {
        String companyName = trimToNull(request.getCompanyName());
        String jobTitle = trimToNull(request.getJobTitle());
        String jobDescription = safe(request.getJobDescription());

        List<String> jdTools = extractKnownTools(jobDescription);
        List<String> knownTools = normalizeList(request.getKnownTools());
        List<String> unknownTools = normalizeList(request.getUnknownTools());
        List<String> keywords = extractKeywords(jobDescription);

        List<String> toolsToHighlight = jdTools.stream()
                .filter(tool -> containsIgnoreCase(knownTools, tool))
                .distinct()
                .collect(Collectors.toList());

        List<String> toolsToAvoidClaiming = jdTools.stream()
                .filter(tool -> containsIgnoreCase(unknownTools, tool))
                .distinct()
                .collect(Collectors.toList());

        List<String> recommendedSkills = buildRecommendedSkills(toolsToHighlight, keywords);

        String tailoredSummary = buildTailoredSummary(
                companyName,
                jobTitle,
                toolsToHighlight,
                keywords,
                request.getAdditionalNotes()
        );

        List<String> optimizedBulletPoints = buildOptimizedBulletPoints(
                jobTitle,
                toolsToHighlight,
                keywords
        );

        Map<String, Object> tailoredContent = new LinkedHashMap<>();
        tailoredContent.put("companyName", companyName);
        tailoredContent.put("jobTitle", jobTitle);
        tailoredContent.put("resumeVersionId", request.getResumeVersionId());
        tailoredContent.put("tailoredSummary", tailoredSummary);
        tailoredContent.put("highlightedTools", toolsToHighlight);
        tailoredContent.put("excludedTools", toolsToAvoidClaiming);
        tailoredContent.put("keywords", keywords);
        tailoredContent.put("recommendedSkills", recommendedSkills);
        tailoredContent.put("optimizedBulletPoints", optimizedBulletPoints);
        tailoredContent.put("generatePreview", request.shouldGeneratePreview());
        tailoredContent.put("knownTools", knownTools);
        tailoredContent.put("unknownTools", unknownTools);
        tailoredContent.put("notes", trimToNull(request.getAdditionalNotes()));

        int beforeScore = calculateBaseAtsScore(jobDescription);
        int afterScore = calculateImprovedAtsScore(jobDescription, jdTools, knownTools);

        ResumeTailorResponse response = ResumeTailorResponse.success("Resume tailored successfully (local)");
        response.setCompanyName(companyName);
        response.setJobTitle(jobTitle);
        response.setResumeVersionId(request.getResumeVersionId());
        response.setDetectedTools(jdTools);
        response.setKeywords(keywords);
        response.setRecommendedSkills(recommendedSkills);
        response.setTailoredContent(tailoredContent);
        response.setAtsScoreBefore(beforeScore);
        response.setAtsScoreAfter(afterScore);
        response.setPreviewGenerated(request.shouldGeneratePreview());
        return response;
    }

    /**
     * Process tool knowledge answer.
     *
     * In a full implementation, this would store the answer in DB and attach it
     * to the job application. Here it returns a structured confirmation payload.
     */
    public ResumeTailorResponse submitToolAnswers(String userId, ToolKnowledgeAnswerRequest request) {
        validateUser(userId);

        if (request == null) {
            throw new ResumeTailoringException("Tool knowledge answer request cannot be null");
        }
        if (request.getJobApplicationId() == null) {
            throw new ResumeTailoringException("Job application id is required");
        }
        if (isBlank(request.getToolName())) {
            throw new ResumeTailoringException("Tool name is required");
        }

        try {
            Map<String, Object> answerData = new LinkedHashMap<>();
            answerData.put("jobApplicationId", request.getJobApplicationId());
            answerData.put("toolName", trimToNull(request.getToolName()));
            answerData.put("required", request.getRequired());
            answerData.put("userKnowsTool", request.getUserKnowsTool());
            answerData.put("userExperienceLevel", normalizeExperienceLevel(request.getUserExperienceLevel(), request.getUserKnowsTool()));
            answerData.put("notes", trimToNull(request.getNotes()));
            answerData.put("decision", buildToolDecision(request));

            ResumeTailorResponse response = ResumeTailorResponse.success("Tool knowledge answer submitted successfully");
            response.setDetectedTools(Collections.singletonList(trimToNull(request.getToolName())));
            response.setRecommendedSkills(
                    request.doesUserKnowTool()
                            ? Collections.singletonList(trimToNull(request.getToolName()))
                            : Collections.emptyList()
            );
            response.setTailoredContent(answerData);
            response.setPreviewGenerated(Boolean.FALSE);
            return response;

        } catch (ResumeTailoringException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResumeTailoringException("Failed to submit tool knowledge answer", ex);
        }
    }

    /**
     * Extract plain text resume content from a ResumeVersion using its sections.
     * Returns empty string if version or sections not found.
     */
    private String extractResumeText(Long resumeVersionId) {
        if (resumeVersionId == null) {
            return "";
        }

        try {
            Optional<ResumeVersion> versionOpt = resumeVersionRepository.findById(resumeVersionId);
            if (versionOpt.isEmpty()) {
                return "";
            }

            List<ResumeSection> sections = resumeSectionRepository.findByResumeVersion_ResumeVersionIdOrderBySectionOrderAsc(resumeVersionId);

            if (sections == null || sections.isEmpty()) {
                return "";
            }

            StringBuilder resumeText = new StringBuilder();
            for (ResumeSection section : sections) {
                if (section == null || section.getSectionType() == null) {
                    continue;
                }

                // Add section title
                resumeText.append("\n").append(section.getSectionTitle()).append("\n");

                // Add section content (prefer plainText for better readability)
                if (section.getPlainText() != null && !section.getPlainText().trim().isEmpty()) {
                    resumeText.append(section.getPlainText()).append("\n");
                }
            }

            return resumeText.toString().trim();
        } catch (Exception e) {
            // Log error and return empty string as fallback
            return "";
        }
    }

    private void validateUser(String userId) {
        if (isBlank(userId)) {
            throw new ResumeTailoringException("Authenticated user is required");
        }
    }

    private void validateTailorRequest(ResumeTailorRequest request) {
        if (request == null) {
            throw new ResumeTailoringException("Resume tailor request cannot be null");
        }
        if (request.getResumeVersionId() == null) {
            throw new ResumeTailoringException("Resume version id is required");
        }
        if (isBlank(request.getCompanyName())) {
            throw new ResumeTailoringException("Company name is required");
        }
        if (isBlank(request.getJobTitle())) {
            throw new ResumeTailoringException("Job title is required");
        }
        if (isBlank(request.getJobDescription())) {
            throw new ResumeTailoringException("Job description is required");
        }
    }

    private List<String> extractKnownTools(String text) {
        String normalized = " " + safe(text).toLowerCase(Locale.ROOT) + " ";
        List<String> matches = new ArrayList<>();

        for (String tool : COMMON_TOOLS) {
            String pattern = "\\b" + Pattern.quote(tool.toLowerCase(Locale.ROOT)) + "\\b";
            if (Pattern.compile(pattern).matcher(normalized).find()) {
                matches.add(tool);
            }
        }

        return matches.stream().distinct().collect(Collectors.toList());
    }

    private List<String> extractKeywords(String text) {
        String normalized = safe(text);

        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "and", "or", "with", "for", "from", "that", "this", "into", "will", "have",
                "has", "had", "you", "your", "our", "their", "they", "them", "are", "were", "was",
                "is", "be", "as", "an", "a", "to", "of", "on", "in", "by", "at", "using", "use",
                "required", "preferred", "responsible", "experience", "work", "candidate", "role",
                "job", "team", "skills", "skill", "years", "year", "plus"
        ));

        Map<String, Integer> frequency = new LinkedHashMap<>();
        Matcher matcher = Pattern.compile("\\b[A-Za-z][A-Za-z+.#/-]{2,}\\b").matcher(normalized);

        while (matcher.find()) {
            String token = matcher.group().trim();
            String key = token.toLowerCase(Locale.ROOT);

            if (!stopWords.contains(key)) {
                frequency.put(token, frequency.getOrDefault(token, 0) + 1);
            }
        }

        return frequency.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(15)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> buildRecommendedSkills(List<String> tools, List<String> keywords) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        result.addAll(defaultList(tools));
        result.addAll(defaultList(keywords).stream().limit(8).collect(Collectors.toList()));
        return new ArrayList<>(result);
    }

    private String buildTailoredSummary(
            String companyName,
            String jobTitle,
            List<String> highlightedTools,
            List<String> keywords,
            String additionalNotes
    ) {
        StringBuilder summary = new StringBuilder();
        summary.append("Results-oriented candidate tailored for the ")
                .append(jobTitle)
                .append(" role at ")
                .append(companyName)
                .append(", with emphasis on relevant technical strengths and ATS-aligned terminology.");

        if (!highlightedTools.isEmpty()) {
            summary.append(" Highlighted tools include ")
                    .append(String.join(", ", highlightedTools.stream().limit(6).collect(Collectors.toList())))
                    .append(".");
        }

        if (!keywords.isEmpty()) {
            summary.append(" Core focus areas include ")
                    .append(String.join(", ", keywords.stream().limit(5).collect(Collectors.toList())))
                    .append(".");
        }

        if (!isBlank(additionalNotes)) {
            summary.append(" Additional context: ").append(additionalNotes.trim()).append(".");
        }

        return summary.toString();
    }

    private List<String> buildOptimizedBulletPoints(
            String jobTitle,
            List<String> toolsToHighlight,
            List<String> keywords
    ) {
        List<String> bullets = new ArrayList<>();

        bullets.add("Tailored resume content toward the " + jobTitle + " position with stronger keyword alignment.");

        if (!toolsToHighlight.isEmpty()) {
            bullets.add("Prioritized relevant tools and technologies such as " +
                    String.join(", ", toolsToHighlight.stream().limit(5).collect(Collectors.toList())) + ".");
        }

        if (!keywords.isEmpty()) {
            bullets.add("Improved ATS match by naturally incorporating role-specific keywords including " +
                    String.join(", ", keywords.stream().limit(6).collect(Collectors.toList())) + ".");
        }

        bullets.add("Preserved honesty by excluding unsupported skills and emphasizing only user-confirmed tools.");
        return bullets;
    }

    private String buildToolDecision(ToolKnowledgeAnswerRequest request) {
        if (request.doesUserKnowTool()) {
            return "INCLUDE_CONFIDENTLY";
        }
        if (request.isRequired()) {
            return "DO_NOT_CLAIM_REQUIRED_TOOL";
        }
        return "OPTIONAL_TOOL_NOT_INCLUDED";
    }

    private int calculateBaseAtsScore(String jobDescription) {
        int score = 45;
        int extra = Math.min(20, extractKnownTools(jobDescription).size() * 2);
        return Math.min(100, score + extra);
    }

    private int calculateImprovedAtsScore(String jobDescription, List<String> jdTools, List<String> knownTools) {
        int base = calculateBaseAtsScore(jobDescription);
        long matchedTools = defaultList(jdTools).stream()
                .filter(tool -> containsIgnoreCase(knownTools, tool))
                .count();

        int improved = base + (int) Math.min(30, matchedTools * 4L);
        return Math.min(100, improved);
    }

    private String normalizeExperienceLevel(String value, Boolean userKnowsTool) {
        if (isBlank(value)) {
            return Boolean.TRUE.equals(userKnowsTool) ? "INTERMEDIATE" : "NONE";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(List<String> values, String target) {
        if (values == null || target == null) {
            return false;
        }
        for (String value : values) {
            if (value != null && value.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private List<String> normalizeList(List<String> input) {
        return defaultList(input).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> defaultList(List<String> input) {
        return input == null ? Collections.emptyList() : input;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
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