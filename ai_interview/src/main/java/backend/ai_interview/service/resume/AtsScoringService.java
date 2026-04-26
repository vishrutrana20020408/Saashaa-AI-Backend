package backend.ai_interview.service.resume;

import backend.ai_interview.service.integration.ai.AiEngineClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ATS Optimization Service
 *
 * Handles:
 * - estimating ATS score for resume text/content
 * - comparing resume content against a job description
 * - extracting missing keywords
 * - generating optimization suggestions
 *
 * NOTE:
 * This service now uses AI-Engine for intelligent ATS scoring.
 * Falls back to rule-based implementation if AI-Engine is unavailable.
 */
@Service
@SuppressWarnings("all")
public class AtsScoringService {

    private final AiEngineClient aiEngineClient;

    public AtsScoringService(AiEngineClient aiEngineClient) {
        this.aiEngineClient = aiEngineClient;
    }

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "or", "with", "for", "from", "that", "this", "into", "will", "have",
            "has", "had", "you", "your", "our", "their", "they", "them", "are", "were", "was",
            "is", "be", "as", "an", "a", "to", "of", "on", "in", "by", "at", "using", "use",
            "required", "preferred", "responsible", "experience", "work", "candidate", "role",
            "job", "team", "skills", "skill", "years", "year", "plus", "must", "should", "can",
            "ability", "knowledge", "understanding", "strong", "good", "excellent", "including"
    ));

    private static final List<String> PRIORITY_KEYWORDS = Arrays.asList(
            "Java", "Spring Boot", "Spring", "Hibernate", "JPA", "MySQL", "PostgreSQL",
            "MongoDB", "Redis", "Kafka", "RabbitMQ", "Docker", "Kubernetes", "AWS",
            "Azure", "GCP", "Jenkins", "Git", "GitHub", "GitLab", "Maven", "Gradle",
            "Microservices", "REST", "REST API", "GraphQL", "JUnit", "Mockito",
            "React", "Next.js", "TypeScript", "JavaScript", "Node.js", "Express",
            "Python", "Django", "Flask", "C++", "C", "HTML", "CSS", "Tailwind",
            "Linux", "CI/CD", "Agile", "Scrum", "Data Structures", "Algorithms",
            "System Design", "Machine Learning", "TensorFlow", "PyTorch", "Pandas",
            "NumPy", "Power BI", "Tableau", "Excel", "Firebase", "Android", "Kotlin"
    );

    /**
     * Estimate ATS score for a resume against a job description.
     * Uses AI-Engine for intelligent scoring, falls back to rule-based if unavailable.
     */
    public int calculateScore(String resumeText, String jobDescription) {
        String safeResume = safe(resumeText);
        String safeJobDescription = safe(jobDescription);

        if (safeResume.isBlank()) {
            return 0;
        }

        try {
            // Try AI-Engine scoring first
            Map<String, Object> payload = aiEngineClient.payloadOf(
                "resume_text", safeResume,
                "job_description", safeJobDescription
            );
            Map<String, Object> response = aiEngineClient.scoreAts(payload);

            // Extract ATS score from response
            Object atsScoreObj = response.get("ats_score");
            if (atsScoreObj instanceof Number) {
                return ((Number) atsScoreObj).intValue();
            }

            // If response doesn't have expected format, fall back to local
            System.err.println("AI-Engine ATS response missing ats_score, falling back to local scoring");
        } catch (Exception e) {
            System.err.println("AI-Engine ATS scoring failed, falling back to local scoring: " + e.getMessage());
        }

        // Fallback to local rule-based scoring
        return calculateScoreLocal(safeResume, safeJobDescription);
    }

    /**
     * Local rule-based ATS scoring (fallback implementation).
     */
    private int calculateScoreLocal(String resumeText, String jobDescription) {
        if (jobDescription.isBlank()) {
            return calculateStandaloneResumeScore(resumeText);
        }

        int score = 0;

        Set<String> jdKeywords = extractKeywords(jobDescription, 30);
        Set<String> resumeKeywords = extractKeywords(resumeText, 60);

        long matchedKeywords = jdKeywords.stream()
                .filter(keyword -> containsIgnoreCase(resumeKeywords, keyword))
                .count();

        int keywordScore = jdKeywords.isEmpty()
                ? 20
                : (int) Math.round((matchedKeywords * 40.0) / jdKeywords.size());

        score += Math.min(40, keywordScore);

        int sectionScore = evaluateSectionPresenceScore(resumeText);
        score += sectionScore;

        int formattingScore = evaluateFormattingScore(resumeText);
        score += formattingScore;

        int contactScore = evaluateContactScore(resumeText);
        score += contactScore;

        int skillScore = evaluatePrioritySkillMatchScore(resumeText, jobDescription);
        score += skillScore;

        return Math.min(100, Math.max(0, score));
    }

    /**
     * Compare ATS score before and after optimization.
     */
    public Map<String, Object> compareScores(String originalResumeText, String optimizedResumeText, String jobDescription) {
        int before = calculateScore(originalResumeText, jobDescription);
        int after = calculateScore(optimizedResumeText, jobDescription);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scoreBefore", before);
        result.put("scoreAfter", after);
        result.put("improvement", after - before);
        result.put("improved", after > before);
        return result;
    }

    /**
     * Extract missing keywords from resume compared to the job description.
     * Uses AI-Engine for intelligent keyword extraction, falls back to rule-based.
     */
    @SuppressWarnings("unchecked")
    public List<String> findMissingKeywords(String resumeText, String jobDescription) {
        String safeResume = safe(resumeText);
        String safeJobDescription = safe(jobDescription);

        if (safeResume.isBlank() || safeJobDescription.isBlank()) {
            return new ArrayList<>();
        }

        try {
            // Try AI-Engine scoring to get missing keywords
            Map<String, Object> payload = aiEngineClient.payloadOf(
                "resume_text", safeResume,
                "job_description", safeJobDescription
            );
            Map<String, Object> response = aiEngineClient.scoreAts(payload);

            // Extract missing keywords from response
            Object missingKeywordsObj = response.get("missing_keywords");
            if (missingKeywordsObj instanceof List) {
                return (List<String>) missingKeywordsObj;
            }
        } catch (Exception e) {
            System.err.println("AI-Engine missing keywords extraction failed, falling back to local: " + e.getMessage());
        }

        // Fallback to local keyword extraction
        Set<String> resumeKeywords = extractKeywords(safeResume, 80);
        Set<String> jdKeywords = extractKeywords(safeJobDescription, 40);

        return jdKeywords.stream()
                .filter(keyword -> !containsIgnoreCase(resumeKeywords, keyword))
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * Return matching keywords between resume and job description.
     * Uses AI-Engine for intelligent keyword matching, falls back to rule-based.
     */
    @SuppressWarnings("unchecked")
    public List<String> findMatchedKeywords(String resumeText, String jobDescription) {
        String safeResume = safe(resumeText);
        String safeJobDescription = safe(jobDescription);

        if (safeResume.isBlank() || safeJobDescription.isBlank()) {
            return new ArrayList<>();
        }

        try {
            // Try AI-Engine scoring to get matched keywords
            Map<String, Object> payload = aiEngineClient.payloadOf(
                "resume_text", safeResume,
                "job_description", safeJobDescription
            );
            Map<String, Object> response = aiEngineClient.scoreAts(payload);

            // Extract matched keywords from response
            Object matchedKeywordsObj = response.get("matched_keywords");
            if (matchedKeywordsObj instanceof List) {
                return (List<String>) matchedKeywordsObj;
            }
        } catch (Exception e) {
            System.err.println("AI-Engine matched keywords extraction failed, falling back to local: " + e.getMessage());
        }

        // Fallback to local keyword extraction
        Set<String> resumeKeywords = extractKeywords(safeResume, 80);
        Set<String> jdKeywords = extractKeywords(safeJobDescription, 40);

        return jdKeywords.stream()
                .filter(keyword -> containsIgnoreCase(resumeKeywords, keyword))
                .collect(Collectors.toList());
    }

    /**
     * Generate optimization suggestions for improving ATS score.
     * Uses AI-Engine for intelligent suggestions, falls back to rule-based.
     */
    @SuppressWarnings("unchecked")
    public List<String> generateSuggestions(String resumeText, String jobDescription) {
        String safeResume = safe(resumeText);
        String safeJobDescription = safe(jobDescription);

        if (safeResume.isBlank()) {
            return Arrays.asList("Add resume content before running ATS optimization.");
        }

        try {
            // Try AI-Engine scoring to get suggestions
            Map<String, Object> payload = aiEngineClient.payloadOf(
                "resume_text", safeResume,
                "job_description", safeJobDescription
            );
            Map<String, Object> response = aiEngineClient.scoreAts(payload);

            // Extract suggestions from response
            Object suggestionsObj = response.get("suggestions");
            if (suggestionsObj instanceof List) {
                return (List<String>) suggestionsObj;
            }
        } catch (Exception e) {
            System.err.println("AI-Engine suggestions generation failed, falling back to local: " + e.getMessage());
        }

        // Fallback to local suggestion generation
        return generateSuggestionsLocal(safeResume, safeJobDescription);
    }

    /**
     * Local rule-based suggestion generation (fallback implementation).
     */
    private List<String> generateSuggestionsLocal(String resumeText, String jobDescription) {
        List<String> suggestions = new ArrayList<>();

        if (!hasEmail(resumeText)) {
            suggestions.add("Add a professional email address to improve contact completeness.");
        }

        if (!hasPhone(resumeText)) {
            suggestions.add("Add a phone number so recruiters can contact you easily.");
        }

        if (!containsSection(resumeText, "summary") && !containsSection(resumeText, "objective")) {
            suggestions.add("Add a short professional summary aligned to the target role.");
        }

        if (!containsExperienceSection(resumeText)) {
            suggestions.add("Add a clear experience section with measurable impact points.");
        }

        if (!containsSection(resumeText, "skills") && !containsSection(resumeText, "technical skills")) {
            suggestions.add("Add a dedicated skills section for ATS readability.");
        }

        if (!containsSection(resumeText, "education")) {
            suggestions.add("Add an education section if relevant to the role.");
        }

        List<String> missingKeywords = findMissingKeywords(resumeText, jobDescription);
        if (!missingKeywords.isEmpty()) {
            suggestions.add("Consider naturally incorporating these job-relevant keywords: " +
                    String.join(", ", missingKeywords.stream().limit(8).collect(Collectors.toList())) + ".");
        }

        List<String> priorityMissing = findMissingPrioritySkills(resumeText, jobDescription);
        if (!priorityMissing.isEmpty()) {
            suggestions.add("Highlight relevant technical skills you genuinely know, such as: " +
                    String.join(", ", priorityMissing.stream().limit(6).collect(Collectors.toList())) + ".");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Resume already shows strong ATS alignment. Focus on stronger achievement bullets and role-specific tailoring.");
        }

        return suggestions;
    }

    /**
     * Produce a detailed ATS analysis payload.
     */
    public Map<String, Object> analyze(String resumeText, String jobDescription) {
        String safeResume = safe(resumeText);
        String safeJobDescription = safe(jobDescription);

        int score = calculateScore(safeResume, safeJobDescription);
        List<String> matchedKeywords = findMatchedKeywords(safeResume, safeJobDescription);
        List<String> missingKeywords = findMissingKeywords(safeResume, safeJobDescription);
        List<String> suggestions = generateSuggestions(safeResume, safeJobDescription);

        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("score", score);
        analysis.put("matchedKeywords", matchedKeywords);
        analysis.put("missingKeywords", missingKeywords);
        analysis.put("suggestions", suggestions);
        analysis.put("resumeKeywordCount", extractKeywords(safeResume, 100).size());
        analysis.put("jobDescriptionKeywordCount", extractKeywords(safeJobDescription, 100).size());
        analysis.put("hasEmail", hasEmail(safeResume));
        analysis.put("hasPhone", hasPhone(safeResume));
        analysis.put("hasSummary", containsSection(safeResume, "summary") || containsSection(safeResume, "objective"));
        analysis.put("hasSkillsSection", containsSection(safeResume, "skills") || containsSection(safeResume, "technical skills"));
        analysis.put("hasExperienceSection", containsExperienceSection(safeResume));
        analysis.put("hasEducationSection", containsSection(safeResume, "education"));
        return analysis;
    }

    /**
     * Standalone resume score when no JD is available.
     */
    public int calculateStandaloneResumeScore(String resumeText) {
        String safeResume = safe(resumeText);

        if (safeResume.isBlank()) {
            return 0;
        }

        int score = 20;
        score += evaluateContactScore(safeResume);
        score += evaluateSectionPresenceScore(safeResume);
        score += evaluateFormattingScore(safeResume);

        Set<String> resumeKeywords = extractKeywords(safeResume, 60);
        int keywordBonus = Math.min(20, resumeKeywords.size() / 2);
        score += keywordBonus;

        return Math.min(100, score);
    }

    private int evaluateContactScore(String resumeText) {
        int score = 0;

        if (hasEmail(resumeText)) {
            score += 5;
        }
        if (hasPhone(resumeText)) {
            score += 5;
        }
        if (hasLink(resumeText)) {
            score += 5;
        }

        return score;
    }

    private int evaluateSectionPresenceScore(String resumeText) {
        int score = 0;

        if (containsSection(resumeText, "summary") || containsSection(resumeText, "objective")) {
            score += 8;
        }
        if (containsSection(resumeText, "skills") || containsSection(resumeText, "technical skills")) {
            score += 10;
        }
        if (containsExperienceSection(resumeText)) {
            score += 12;
        }
        if (containsSection(resumeText, "projects")) {
            score += 5;
        }
        if (containsSection(resumeText, "education")) {
            score += 5;
        }

        return Math.min(30, score);
    }

    private int evaluateFormattingScore(String resumeText) {
        int score = 0;

        if (resumeText.length() >= 300) {
            score += 5;
        }
        if (resumeText.length() >= 800) {
            score += 5;
        }
        if (resumeText.contains("\n")) {
            score += 5;
        }

        return Math.min(15, score);
    }

    private int evaluatePrioritySkillMatchScore(String resumeText, String jobDescription) {
        List<String> relevantPrioritySkills = PRIORITY_KEYWORDS.stream()
                .filter(skill -> containsIgnoreCase(jobDescription, skill))
                .collect(Collectors.toList());

        if (relevantPrioritySkills.isEmpty()) {
            return 10;
        }

        long matched = relevantPrioritySkills.stream()
                .filter(skill -> containsIgnoreCase(resumeText, skill))
                .count();

        return (int) Math.min(15, Math.round((matched * 15.0) / relevantPrioritySkills.size()));
    }

    private List<String> findMissingPrioritySkills(String resumeText, String jobDescription) {
        return PRIORITY_KEYWORDS.stream()
                .filter(skill -> containsIgnoreCase(jobDescription, skill))
                .filter(skill -> !containsIgnoreCase(resumeText, skill))
                .distinct()
                .collect(Collectors.toList());
    }

    private Set<String> extractKeywords(String text, int limit) {
        if (text == null || text.isBlank()) {
            return new LinkedHashSet<>();
        }

        Map<String, Integer> frequency = new HashMap<>();
        Matcher matcher = Pattern.compile("\\b[A-Za-z][A-Za-z0-9+.#/-]{2,}\\b").matcher(text);

        while (matcher.find()) {
            String token = matcher.group().trim();
            String lower = token.toLowerCase(Locale.ROOT);

            if (!STOP_WORDS.contains(lower)) {
                frequency.put(token, frequency.getOrDefault(token, 0) + 1);
            }
        }

        LinkedHashSet<String> result = new LinkedHashSet<>();

        PRIORITY_KEYWORDS.stream()
                .filter(skill -> containsIgnoreCase(text, skill))
                .forEach(result::add);

        frequency.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .filter(keyword -> !containsIgnoreCase(result, keyword))
                .limit(Math.max(0, limit - result.size()))
                .forEach(result::add);

        return result.stream().limit(limit).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean hasEmail(String text) {
        return Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
                .matcher(safe(text))
                .find();
    }

    private boolean hasPhone(String text) {
        return Pattern.compile("(\\+?\\d{1,3}[\\s-]?)?(\\d{10})")
                .matcher(safe(text).replaceAll("[()]", " "))
                .find();
    }

    private boolean hasLink(String text) {
        return Pattern.compile("(https?://\\S+|www\\.\\S+|linkedin\\.com/\\S+|github\\.com/\\S+)")
                .matcher(safe(text).toLowerCase(Locale.ROOT))
                .find();
    }

    private boolean containsExperienceSection(String text) {
        return containsSection(text, "experience")
                || containsSection(text, "work experience")
                || containsSection(text, "professional experience");
    }

    private boolean containsSection(String text, String sectionName) {
        String safeText = safe(text).toLowerCase(Locale.ROOT);
        String normalizedSection = safe(sectionName).toLowerCase(Locale.ROOT);

        Pattern pattern = Pattern.compile("(?m)^\\s*" + Pattern.quote(normalizedSection) + "\\s*:?\\s*$");
        return pattern.matcher(safeText).find();
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }

        String pattern = "\\b" + Pattern.quote(keyword.toLowerCase(Locale.ROOT)) + "\\b";
        return Pattern.compile(pattern).matcher(text.toLowerCase(Locale.ROOT)).find();
    }

    private boolean containsIgnoreCase(Collection<String> values, String target) {
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

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}