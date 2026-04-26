package backend.ai_interview.service.resume;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Skill Extraction Service
 *
 * Handles:
 * - extracting tools / technologies / skills from text
 * - extracting role-relevant keywords from job descriptions
 * - separating detected known technical skills from general keywords
 *
 * Supported inputs:
 * - resume raw text
 * - job description
 * - onboarding text / notes
 *
 * NOTE:
 * This is a rule-based starter implementation.
 * You can later enhance it using:
 * - NLP models
 * - embeddings
 * - external skill taxonomy
 * - AI/LLM-based parsing
 *
 * Latest project alignment:
 * - supports resume tailoring flow
 * - supports resume parser / profile extraction integration
 * - supports resume-vs-job comparison for ATS-oriented workflows
 */
@Service
@SuppressWarnings("all")
public class SkillExtractionService {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "or", "with", "for", "from", "that", "this", "into", "will", "have",
            "has", "had", "you", "your", "our", "their", "they", "them", "are", "were", "was",
            "is", "be", "as", "an", "a", "to", "of", "on", "in", "by", "at", "using", "use",
            "required", "preferred", "responsible", "experience", "work", "candidate", "role",
            "job", "team", "skills", "skill", "years", "year", "plus", "must", "should", "can",
            "ability", "knowledge", "understanding", "strong", "good", "excellent", "including",
            "build", "develop", "design", "support", "maintain", "implement", "workflows",
            "projects", "project", "application", "applications", "system", "systems"
    ));

    private static final LinkedHashSet<String> KNOWN_SKILLS = new LinkedHashSet<>(Arrays.asList(
            "Java",
            "Spring",
            "Spring Boot",
            "Spring Security",
            "Hibernate",
            "JPA",
            "Servlets",
            "JDBC",
            "MySQL",
            "PostgreSQL",
            "Oracle",
            "SQL",
            "MongoDB",
            "Redis",
            "Firebase",
            "Kafka",
            "RabbitMQ",
            "Docker",
            "Kubernetes",
            "AWS",
            "Azure",
            "GCP",
            "EC2",
            "S3",
            "Lambda",
            "Jenkins",
            "CI/CD",
            "Git",
            "GitHub",
            "GitLab",
            "Bitbucket",
            "Maven",
            "Gradle",
            "Linux",
            "Bash",
            "Shell Scripting",
            "Microservices",
            "REST",
            "REST API",
            "GraphQL",
            "SOAP",
            "JUnit",
            "Mockito",
            "Testing",
            "Unit Testing",
            "Integration Testing",
            "React",
            "Next.js",
            "Angular",
            "Vue",
            "JavaScript",
            "TypeScript",
            "Node.js",
            "Express",
            "HTML",
            "CSS",
            "Tailwind",
            "Bootstrap",
            "Python",
            "Django",
            "Flask",
            "FastAPI",
            "Pandas",
            "NumPy",
            "TensorFlow",
            "PyTorch",
            "Scikit-learn",
            "Machine Learning",
            "Deep Learning",
            "Data Analysis",
            "C",
            "C++",
            "C#",
            "Kotlin",
            "Android",
            "PHP",
            "Laravel",
            "Go",
            "Rust",
            "Power BI",
            "Tableau",
            "Excel",
            "Figma",
            "Agile",
            "Scrum",
            "Data Structures",
            "Algorithms",
            "System Design",
            "OOP",
            "Object Oriented Programming",
            "Design Patterns",
            "Problem Solving"
    ));

    /**
     * Extract all recognized technical skills from text.
     */
    public List<String> extractSkills(String text) {
        String safeText = safe(text);
        if (safeText.isBlank()) {
            return Collections.emptyList();
        }

        String normalized = " " + safeText.toLowerCase(Locale.ROOT) + " ";
        LinkedHashSet<String> matches = new LinkedHashSet<>();

        for (String skill : KNOWN_SKILLS) {
            String pattern = buildSkillPattern(skill);
            if (Pattern.compile(pattern).matcher(normalized).find()) {
                matches.add(skill);
            }
        }

        return new ArrayList<>(matches);
    }

    /**
     * Extract top keywords from text.
     * Useful for job descriptions and ATS matching.
     */
    public List<String> extractKeywords(String text) {
        return extractKeywords(text, 20);
    }

    /**
     * Extract top keywords from text with configurable limit.
     */
    public List<String> extractKeywords(String text, int limit) {
        String safeText = safe(text);
        if (safeText.isBlank() || limit <= 0) {
            return Collections.emptyList();
        }

        Map<String, Integer> frequency = new LinkedHashMap<>();
        Matcher matcher = Pattern.compile("\\b[A-Za-z][A-Za-z0-9+.#/-]{2,}\\b").matcher(safeText);

        while (matcher.find()) {
            String token = matcher.group().trim();
            String lower = token.toLowerCase(Locale.ROOT);

            if (!STOP_WORDS.contains(lower)) {
                frequency.put(token, frequency.getOrDefault(token, 0) + 1);
            }
        }

        return frequency.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Extract both technical skills and general keywords together.
     */
    public Map<String, Object> extractSkillSummary(String text) {
        String safeText = safe(text);

        List<String> skills = extractSkills(safeText);
        List<String> keywords = extractKeywords(safeText, 20);
        List<String> missingFromKeywords = keywords.stream()
                .filter(keyword -> !containsIgnoreCase(skills, keyword))
                .limit(10)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("skills", skills);
        result.put("keywords", keywords);
        result.put("nonSkillKeywords", missingFromKeywords);
        result.put("skillCount", skills.size());
        result.put("keywordCount", keywords.size());

        return result;
    }

    /**
     * Extract skills relevant to a job description.
     */
    public List<String> extractSkillsFromJobDescription(String jobDescription) {
        return extractSkills(jobDescription);
    }

    /**
     * Extract skills relevant to a resume.
     */
    public List<String> extractSkillsFromResume(String resumeText) {
        return extractSkills(resumeText);
    }

    /**
     * Return missing skills from resume compared to job description.
     */
    public List<String> findMissingSkills(String resumeText, String jobDescription) {
        List<String> resumeSkills = extractSkills(resumeText);
        List<String> jdSkills = extractSkills(jobDescription);

        return jdSkills.stream()
                .filter(skill -> !containsIgnoreCase(resumeSkills, skill))
                .collect(Collectors.toList());
    }

    /**
     * Return matched skills between resume and job description.
     */
    public List<String> findMatchedSkills(String resumeText, String jobDescription) {
        List<String> resumeSkills = extractSkills(resumeText);
        List<String> jdSkills = extractSkills(jobDescription);

        return jdSkills.stream()
                .filter(skill -> containsIgnoreCase(resumeSkills, skill))
                .collect(Collectors.toList());
    }

    /**
     * Return a full comparison payload for resume vs job description.
     */
    public Map<String, Object> compareResumeWithJobDescription(String resumeText, String jobDescription) {
        List<String> resumeSkills = extractSkills(resumeText);
        List<String> jobSkills = extractSkills(jobDescription);
        List<String> matchedSkills = findMatchedSkills(resumeText, jobDescription);
        List<String> missingSkills = findMissingSkills(resumeText, jobDescription);
        List<String> keywords = extractKeywords(jobDescription, 20);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resumeSkills", resumeSkills);
        result.put("jobSkills", jobSkills);
        result.put("matchedSkills", matchedSkills);
        result.put("missingSkills", missingSkills);
        result.put("jobKeywords", keywords);
        result.put("matchPercentage", calculateMatchPercentage(jobSkills, matchedSkills));

        return result;
    }

    /**
     * Normalize a mixed list of skill names from frontend or extracted text.
     */
    public List<String> normalizeSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String skill : skills) {
            if (skill == null) {
                continue;
            }

            String cleaned = skill.trim();
            if (!cleaned.isEmpty()) {
                normalized.add(cleaned);
            }
        }

        return new ArrayList<>(normalized);
    }

    private int calculateMatchPercentage(List<String> jobSkills, List<String> matchedSkills) {
        if (jobSkills == null || jobSkills.isEmpty()) {
            return 0;
        }

        double percentage = (matchedSkills.size() * 100.0) / jobSkills.size();
        return (int) Math.round(Math.min(100, percentage));
    }

    private String buildSkillPattern(String skill) {
        String normalized = skill.toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "c++" -> "(?<![a-z0-9])c\\+\\+(?![a-z0-9])";
            case "c#" -> "(?<![a-z0-9])c#(?![a-z0-9])";
            case "node.js" -> "(?<![a-z0-9])node\\.js(?![a-z0-9])";
            case "next.js" -> "(?<![a-z0-9])next\\.js(?![a-z0-9])";
            case "ci/cd" -> "(?<![a-z0-9])ci/cd(?![a-z0-9])";
            case "rest api" -> "\\brest\\s+api\\b";
            case "object oriented programming" -> "\\bobject\\s+oriented\\s+programming\\b";
            case "spring boot" -> "\\bspring\\s+boot\\b";
            case "spring security" -> "\\bspring\\s+security\\b";
            case "shell scripting" -> "\\bshell\\s+scripting\\b";
            case "data structures" -> "\\bdata\\s+structures\\b";
            case "system design" -> "\\bsystem\\s+design\\b";
            case "machine learning" -> "\\bmachine\\s+learning\\b";
            case "deep learning" -> "\\bdeep\\s+learning\\b";
            case "data analysis" -> "\\bdata\\s+analysis\\b";
            case "problem solving" -> "\\bproblem\\s+solving\\b";
            case "design patterns" -> "\\bdesign\\s+patterns\\b";
            case "unit testing" -> "\\bunit\\s+testing\\b";
            case "integration testing" -> "\\bintegration\\s+testing\\b";
            default -> "\\b" + Pattern.quote(normalized) + "\\b";
        };
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