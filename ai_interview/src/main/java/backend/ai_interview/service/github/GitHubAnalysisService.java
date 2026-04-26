package backend.ai_interview.service.github;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.ai_interview.dto.request.GitHubProjectAnalysisRequest;
import backend.ai_interview.dto.response.GitHubProjectAnalysisResponse;
import backend.ai_interview.exception.GitHubAnalysisException;

/**
 * GitHubAnalysisService
 *
 * Service responsible for analyzing GitHub repository/project input and
 * returning a structured analysis response for:
 * - resume enrichment
 * - interview preparation
 * - project credibility understanding
 * - project-based interview question generation
 *
 * -------------------------------------------------------------------------
 * CURRENT DESIGN
 * -------------------------------------------------------------------------
 * This implementation is intentionally backend-safe and self-contained.
 * It works immediately without external GitHub API integration.
 *
 * Right now it performs:
 * - repository URL parsing
 * - heuristic technology detection
 * - heuristic interview insights generation
 * - role relevance estimation
 * - project quality scoring
 *
 * -------------------------------------------------------------------------
 * FUTURE INTEGRATION POINTS
 * -------------------------------------------------------------------------
 * Later you can replace/extend this service to call:
 * - GitHub REST API
 * - GitHub GraphQL API
 * - AI-engine analysis endpoints
 *
 * Suggested future split:
 * - GitHub metadata fetch client
 * - README fetch client
 * - AI semantic analyzer
 *
 * -------------------------------------------------------------------------
 * IMPORTANT
 * -------------------------------------------------------------------------
 * This service currently does NOT require GitHub tokens.
 * Add token-based access later only when you integrate actual GitHub fetching.
 */
@Service
@SuppressWarnings("all")
@Transactional(readOnly = true)
public class GitHubAnalysisService {

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile(
            "^https?://(?:www\\.)?github\\.com/([^/\\s]+)/([^/\\s#?]+)(?:/.*)?$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Analyze a general GitHub project/repository.
     */
    public GitHubProjectAnalysisResponse analyzeProject(GitHubProjectAnalysisRequest request) {
        validateRequest(request);

        ParsedGitHubRepo parsedRepo = parseRepository(request.getRepositoryUrl());

        GitHubProjectAnalysisResponse response = new GitHubProjectAnalysisResponse();
        response.setRepositoryUrl(request.getRepositoryUrl().trim());
        response.setOwner(firstNonBlank(request.getOwner(), parsedRepo.owner()));
        response.setRepositoryName(firstNonBlank(request.getRepositoryName(), parsedRepo.repositoryName()));
        response.setProjectTitle(firstNonBlank(request.getProjectTitle(), humanizeRepoName(parsedRepo.repositoryName())));
        response.setAnalysisMode(firstNonBlank(request.getAnalysisMode(), "BASIC"));
        response.setResumeId(request.getResumeId());
        response.setResumeVersionId(request.getResumeVersionId());
        response.setResumeProjectId(request.getResumeProjectId());
        response.setTargetRole(trimToNull(request.getTargetRole()));

        List<String> technologies = detectTechnologies(request, parsedRepo);
        List<String> skills = extractSkills(technologies, request);

        int structureScore = scoreStructure(request, parsedRepo, technologies);
        int documentationScore = scoreDocumentation(request);
        int technicalDepthScore = scoreTechnicalDepth(technologies);
        int originalityScore = scoreOriginality(request, parsedRepo);
        int resumeRelevanceScore = scoreResumeRelevance(request, technologies);
        int roleRelevanceScore = scoreRoleRelevance(request, technologies);

        int overallScore = Math.round(
                (structureScore
                        + documentationScore
                        + technicalDepthScore
                        + originalityScore
                        + resumeRelevanceScore
                        + roleRelevanceScore) / 6.0f
        );

        response.setAnalyzed(Boolean.TRUE);
        response.setSummary(buildSummary(request, parsedRepo, technologies, overallScore));
        response.setDetailedAnalysis(buildDetailedAnalysis(request, parsedRepo, technologies, overallScore));
        response.setReadmeSummary(buildReadmeSummary(request, parsedRepo, technologies));
        response.setProjectPurpose(inferProjectPurpose(request, parsedRepo, technologies));
        response.setComplexityLevel(resolveComplexityLevel(technicalDepthScore, technologies.size()));

        response.setOverallScore(overallScore);
        response.setResumeRelevanceScore(resumeRelevanceScore);
        response.setRoleRelevanceScore(roleRelevanceScore);
        response.setStructureScore(structureScore);
        response.setDocumentationScore(documentationScore);
        response.setTechnicalDepthScore(technicalDepthScore);
        response.setOriginalityScore(originalityScore);

        response.setPrimaryLanguage(detectPrimaryLanguage(technologies));
        response.setDetectedTechnologies(technologies);
        response.setExtractedSkills(skills);
        response.setRepositoryTopics(buildRepositoryTopics(technologies, request));

        response.setStrengths(buildStrengths(parsedRepo, technologies, overallScore));
        response.setWeaknesses(buildWeaknesses(request, technologies, overallScore));
        response.setRisks(buildRisks(request, technologies));
        response.setImprovementSuggestions(buildImprovementSuggestions(technologies, overallScore));
        response.setInterviewTalkingPoints(buildInterviewTalkingPoints(technologies));
        response.setSuggestedInterviewQuestions(buildSuggestedInterviewQuestions(technologies));
        response.setKeyConceptsToExplain(buildKeyConceptsToExplain(technologies));
        response.setMissingOrUnclearTechnologies(buildMissingOrUnclearTechnologies(request, technologies));
        response.setNotableFeatures(buildNotableFeatures(parsedRepo, technologies));
        response.setArchitectureNotes(buildArchitectureNotes(parsedRepo, technologies));
        response.setRelevantFiles(buildRelevantFiles(technologies));
        response.setMetadata(buildRepositoryMetadata(request, technologies));

        response.setReadmeAnalyzed(defaultBoolean(request.getIncludeReadmeAnalysis(), Boolean.TRUE));
        response.setRepositoryMetadataAnalyzed(defaultBoolean(request.getIncludeRepositoryMetadata(), Boolean.TRUE));
        response.setFileStructureAnalyzed(defaultBoolean(request.getIncludeFileStructureAnalysis(), Boolean.TRUE));
        response.setAnalyzedAt(LocalDateTime.now());
        response.setMessage("GitHub project analyzed successfully");
        response.setWarnings(buildWarnings(request));

        return response;
    }

    /**
     * Analyze a GitHub project specifically in resume-project context.
     *
     * This method enhances the general analysis with stronger resume/interview
     * framing, but still uses the same core engine.
     */
    public GitHubProjectAnalysisResponse analyzeResumeProject(GitHubProjectAnalysisRequest request) {
        GitHubProjectAnalysisResponse response = analyzeProject(request);

        // Resume-project context adjustments
        List<String> talkingPoints = new ArrayList<>(safeList(response.getInterviewTalkingPoints()));
        if (hasText(request.getProjectTitle())) {
            talkingPoints.add("Be ready to explain why the resume project title matches the actual repository scope.");
        }
        if (request.getResumeProjectId() != null) {
            talkingPoints.add("This project is linked to a resume entry, so consistency between resume claims and repository content matters.");
        }
        response.setInterviewTalkingPoints(distinct(talkingPoints));

        List<String> risks = new ArrayList<>(safeList(response.getRisks()));
        risks.add("If resume claims exceed what the repository demonstrates, interviewers may question credibility.");
        response.setRisks(distinct(risks));

        List<String> suggestions = new ArrayList<>(safeList(response.getImprovementSuggestions()));
        suggestions.add("Ensure the resume description clearly matches the repository purpose, stack, and impact.");
        response.setImprovementSuggestions(distinct(suggestions));

        response.setMessage("Resume-linked GitHub project analyzed successfully");
        return response;
    }

    // ---------------------------------------------------------------------
    // Validation + parsing
    // ---------------------------------------------------------------------

    private void validateRequest(GitHubProjectAnalysisRequest request) {
        if (request == null) {
            throw new GitHubAnalysisException("GitHub analysis request must not be null");
        }
        if (!hasText(request.getRepositoryUrl())) {
            throw GitHubAnalysisException.invalidRepositoryUrl(request.getRepositoryUrl());
        }
    }

    private ParsedGitHubRepo parseRepository(String repositoryUrl) {
        String cleaned = repositoryUrl.trim();

        Matcher matcher = GITHUB_URL_PATTERN.matcher(cleaned);
        if (!matcher.matches()) {
            throw GitHubAnalysisException.invalidRepositoryUrl(repositoryUrl);
        }

        String owner = matcher.group(1);
        String repositoryName = matcher.group(2);

        if (repositoryName.endsWith(".git")) {
            repositoryName = repositoryName.substring(0, repositoryName.length() - 4);
        }

        if (!hasText(owner) || !hasText(repositoryName)) {
            throw GitHubAnalysisException.invalidRepositoryUrl(repositoryUrl);
        }

        return new ParsedGitHubRepo(owner, repositoryName);
    }

    // ---------------------------------------------------------------------
    // Core analysis logic
    // ---------------------------------------------------------------------

    private List<String> detectTechnologies(GitHubProjectAnalysisRequest request, ParsedGitHubRepo repo) {
        Set<String> detected = new LinkedHashSet<>();

        addDeclaredTechnologies(detected, request.getDeclaredTechnologies());

        String combined = buildDetectionText(request, repo).toLowerCase(Locale.ROOT);

        detectIfContains(combined, detected, "java", "Java");
        detectIfContains(combined, detected, "spring", "Spring Boot");
        detectIfContains(combined, detected, "spring boot", "Spring Boot");
        detectIfContains(combined, detected, "hibernate", "Hibernate");
        detectIfContains(combined, detected, "jpa", "JPA");
        detectIfContains(combined, detected, "maven", "Maven");
        detectIfContains(combined, detected, "gradle", "Gradle");
        detectIfContains(combined, detected, "python", "Python");
        detectIfContains(combined, detected, "fastapi", "FastAPI");
        detectIfContains(combined, detected, "flask", "Flask");
        detectIfContains(combined, detected, "django", "Django");
        detectIfContains(combined, detected, "javascript", "JavaScript");
        detectIfContains(combined, detected, "typescript", "TypeScript");
        detectIfContains(combined, detected, "react", "React");
        detectIfContains(combined, detected, "next", "Next.js");
        detectIfContains(combined, detected, "node", "Node.js");
        detectIfContains(combined, detected, "express", "Express.js");
        detectIfContains(combined, detected, "mysql", "MySQL");
        detectIfContains(combined, detected, "postgres", "PostgreSQL");
        detectIfContains(combined, detected, "mongo", "MongoDB");
        detectIfContains(combined, detected, "redis", "Redis");
        detectIfContains(combined, detected, "aws", "AWS");
        detectIfContains(combined, detected, "s3", "AWS S3");
        detectIfContains(combined, detected, "docker", "Docker");
        detectIfContains(combined, detected, "kubernetes", "Kubernetes");
        detectIfContains(combined, detected, "jwt", "JWT");
        detectIfContains(combined, detected, "rest api", "REST API");
        detectIfContains(combined, detected, "websocket", "WebSocket");
        detectIfContains(combined, detected, "ai", "AI");
        detectIfContains(combined, detected, "llm", "LLM");
        detectIfContains(combined, detected, "machine learning", "Machine Learning");
        detectIfContains(combined, detected, "nlp", "NLP");

        // Heuristic fallback from repo name
        String repoName = repo.repositoryName().toLowerCase(Locale.ROOT);
        if (repoName.contains("interview")) {
            detected.add("Interview System");
        }
        if (repoName.contains("resume")) {
            detected.add("Resume Management");
        }
        if (repoName.contains("portfolio")) {
            detected.add("Portfolio");
        }
        if (repoName.contains("api")) {
            detected.add("REST API");
        }

        if (detected.isEmpty()) {
            detected.add("General Software Development");
        }

        return new ArrayList<>(detected);
    }

    private List<String> extractSkills(List<String> technologies, GitHubProjectAnalysisRequest request) {
        Set<String> skills = new LinkedHashSet<>();

        for (String tech : safeList(technologies)) {
            switch (tech.toLowerCase(Locale.ROOT)) {
                case "java" -> skills.add("Java Development");
                case "spring boot" -> {
                    skills.add("Backend Development");
                    skills.add("Spring Boot Development");
                    skills.add("REST API Development");
                }
                case "react" -> {
                    skills.add("Frontend Development");
                    skills.add("Component-based UI Development");
                }
                case "next.js" -> {
                    skills.add("Frontend Development");
                    skills.add("Full Stack Web Development");
                }
                case "mysql", "postgresql", "mongodb" -> {
                    skills.add("Database Design");
                    skills.add("Data Modeling");
                }
                case "docker" -> skills.add("Containerization");
                case "kubernetes" -> skills.add("Deployment Orchestration");
                case "aws", "aws s3" -> {
                    skills.add("Cloud Integration");
                    skills.add("Cloud Storage Management");
                }
                case "websocket" -> skills.add("Real-time Communication");
                case "ai", "llm", "machine learning", "nlp" -> {
                    skills.add("AI Integration");
                    skills.add("Applied AI Systems");
                }
                default -> {
                    // retain useful tech as a generic skill
                    skills.add(tech);
                }
            }
        }

        if (hasText(request.getTargetRole())) {
            skills.add(request.getTargetRole());
        }

        return new ArrayList<>(skills);
    }

    private int scoreStructure(GitHubProjectAnalysisRequest request, ParsedGitHubRepo repo, List<String> technologies) {
        int score = 55;

        if (hasText(request.getProjectDescription())) score += 8;
        if (!safeList(technologies).isEmpty()) score += 8;
        if (hasText(request.getBranchName())) score += 3;
        if (defaultBoolean(request.getIncludeFileStructureAnalysis(), Boolean.TRUE)) score += 8;
        if (repo.repositoryName().contains("-") || repo.repositoryName().contains("_")) score += 4;

        return clamp(score, 0, 100);
    }

    private int scoreDocumentation(GitHubProjectAnalysisRequest request) {
        int score = 50;

        if (defaultBoolean(request.getIncludeReadmeAnalysis(), Boolean.TRUE)) score += 15;
        if (hasText(request.getProjectDescription())) score += 15;
        if (hasText(request.getProjectTitle())) score += 5;
        if (hasText(request.getJobDescription())) score += 5;

        return clamp(score, 0, 100);
    }

    private int scoreTechnicalDepth(List<String> technologies) {
        int score = 45;
        int techCount = safeList(technologies).size();

        score += Math.min(techCount * 5, 30);

        if (containsTech(technologies, "Spring Boot")) score += 6;
        if (containsTech(technologies, "AWS")) score += 4;
        if (containsTech(technologies, "AWS S3")) score += 4;
        if (containsTech(technologies, "WebSocket")) score += 4;
        if (containsTech(technologies, "AI")) score += 4;
        if (containsTech(technologies, "LLM")) score += 3;

        return clamp(score, 0, 100);
    }

    private int scoreOriginality(GitHubProjectAnalysisRequest request, ParsedGitHubRepo repo) {
        int score = 50;

        String combined = buildDetectionText(request, repo).toLowerCase(Locale.ROOT);

        if (combined.contains("ai interview")) score += 18;
        else if (combined.contains("resume")) score += 10;
        else if (combined.contains("real-time")) score += 8;
        else if (combined.contains("system")) score += 6;

        return clamp(score, 0, 100);
    }

    private int scoreResumeRelevance(GitHubProjectAnalysisRequest request, List<String> technologies) {
        int score = 55;

        if (request.getResumeId() != null || request.getResumeVersionId() != null || request.getResumeProjectId() != null) {
            score += 15;
        }
        if (hasText(request.getProjectTitle())) score += 10;
        if (!safeList(request.getDeclaredTechnologies()).isEmpty()) score += 10;
        if (!safeList(technologies).isEmpty()) score += 5;

        return clamp(score, 0, 100);
    }

    private int scoreRoleRelevance(GitHubProjectAnalysisRequest request, List<String> technologies) {
        if (!hasText(request.getTargetRole()) && !hasText(request.getJobDescription())) {
            return 60;
        }

        String roleContext = (defaultString(request.getTargetRole()) + " " + defaultString(request.getJobDescription()))
                .toLowerCase(Locale.ROOT);

        int score = 45;

        for (String tech : safeList(technologies)) {
            if (roleContext.contains(tech.toLowerCase(Locale.ROOT))) {
                score += 8;
            }
        }

        if (roleContext.contains("backend") && containsTech(technologies, "Spring Boot")) score += 10;
        if (roleContext.contains("frontend") && (containsTech(technologies, "React") || containsTech(technologies, "Next.js"))) score += 10;
        if (roleContext.contains("java") && containsTech(technologies, "Java")) score += 10;
        if (roleContext.contains("cloud") && (containsTech(technologies, "AWS") || containsTech(technologies, "AWS S3"))) score += 8;

        return clamp(score, 0, 100);
    }

    // ---------------------------------------------------------------------
    // Response text builders
    // ---------------------------------------------------------------------

    private String buildSummary(
            GitHubProjectAnalysisRequest request,
            ParsedGitHubRepo repo,
            List<String> technologies,
            int overallScore
    ) {
        return "Repository "
                + repo.owner()
                + "/"
                + repo.repositoryName()
                + " appears to represent a "
                + inferProjectPurpose(request, repo, technologies).toLowerCase(Locale.ROOT)
                + " project with an estimated overall analysis score of "
                + overallScore
                + "/100. The repository likely demonstrates work around "
                + String.join(", ", safeTake(technologies, 5))
                + ".";
    }

    private String buildDetailedAnalysis(
            GitHubProjectAnalysisRequest request,
            ParsedGitHubRepo repo,
            List<String> technologies,
            int overallScore
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("This project appears to be associated with ");
        sb.append(firstNonBlank(request.getProjectTitle(), humanizeRepoName(repo.repositoryName())));
        sb.append(". ");

        if (hasText(request.getProjectDescription())) {
            sb.append("Based on the provided description, the repository likely focuses on ");
            sb.append(request.getProjectDescription().trim());
            sb.append(". ");
        }

        sb.append("Detected or inferred technologies include ");
        sb.append(String.join(", ", safeTake(technologies, 8)));
        sb.append(". ");

        if (hasText(request.getTargetRole())) {
            sb.append("For the target role of ");
            sb.append(request.getTargetRole().trim());
            sb.append(", this project can be positioned as evidence of practical implementation ability. ");
        }

        sb.append("Overall, the project seems ");
        if (overallScore >= 80) {
            sb.append("strong and interview-worthy, especially if the candidate can clearly explain architecture, decisions, and outcomes.");
        } else if (overallScore >= 65) {
            sb.append("moderately strong, but it would benefit from clearer documentation, stronger explanation of impact, or deeper technical articulation.");
        } else {
            sb.append("somewhat promising, but likely needs better structure, documentation, or clearer demonstration of technical depth.");
        }

        return sb.toString();
    }

    private String buildReadmeSummary(
            GitHubProjectAnalysisRequest request,
            ParsedGitHubRepo repo,
            List<String> technologies
    ) {
        if (!defaultBoolean(request.getIncludeReadmeAnalysis(), Boolean.TRUE)) {
            return null;
        }

        if (hasText(request.getProjectDescription())) {
            return "The provided project description suggests that the README would likely explain the project purpose, the core technology stack, and the main use case around "
                    + inferProjectPurpose(request, repo, technologies).toLowerCase(Locale.ROOT)
                    + ".";
        }

        return "README content was not fetched directly in this version, but based on repository naming and context, it should ideally explain setup, architecture, features, and use cases.";
    }

    private String inferProjectPurpose(
            GitHubProjectAnalysisRequest request,
            ParsedGitHubRepo repo,
            List<String> technologies
    ) {
        String text = buildDetectionText(request, repo).toLowerCase(Locale.ROOT);

        if (text.contains("interview")) {
            return "AI Interview Preparation / Interview Management";
        }
        if (text.contains("resume")) {
            return "Resume Management / Resume Analysis";
        }
        if (text.contains("portfolio")) {
            return "Portfolio / Personal Showcase";
        }
        if (text.contains("ecommerce") || text.contains("shop")) {
            return "E-commerce Application";
        }
        if (containsTech(technologies, "Spring Boot") && containsTech(technologies, "React")) {
            return "Full Stack Web Application";
        }
        if (containsTech(technologies, "Spring Boot")) {
            return "Backend Service Application";
        }
        if (containsTech(technologies, "React") || containsTech(technologies, "Next.js")) {
            return "Frontend Web Application";
        }
        return "Software Development Project";
    }

    private String resolveComplexityLevel(int technicalDepthScore, int techCount) {
        if (technicalDepthScore >= 80 || techCount >= 8) {
            return "ADVANCED";
        }
        if (technicalDepthScore >= 60 || techCount >= 4) {
            return "INTERMEDIATE";
        }
        return "BEGINNER";
    }

    private List<String> buildRepositoryTopics(List<String> technologies, GitHubProjectAnalysisRequest request) {
        Set<String> topics = new LinkedHashSet<>();

        for (String tech : safeList(technologies)) {
            topics.add(toTopic(tech));
        }

        if (hasText(request.getTargetRole())) {
            topics.add(toTopic(request.getTargetRole()));
        }

        if (defaultBoolean(request.getGenerateInterviewInsights(), Boolean.TRUE)) {
            topics.add("interview-prep");
        }

        return new ArrayList<>(topics);
    }

    private List<String> buildStrengths(
            ParsedGitHubRepo repo,
            List<String> technologies,
            int overallScore
    ) {
        List<String> strengths = new ArrayList<>();

        if (overallScore >= 75) {
            strengths.add("Project appears strong enough to be discussed confidently in interviews");
        }
        if (safeList(technologies).size() >= 4) {
            strengths.add("Repository suggests exposure to multiple technologies");
        }
        if (containsTech(technologies, "Spring Boot")) {
            strengths.add("Shows backend development capability");
        }
        if (containsTech(technologies, "React") || containsTech(technologies, "Next.js")) {
            strengths.add("Shows frontend or full stack capability");
        }
        if (containsTech(technologies, "AWS") || containsTech(technologies, "AWS S3")) {
            strengths.add("Demonstrates cloud integration awareness");
        }
        if (repo.repositoryName().toLowerCase(Locale.ROOT).contains("interview")
                || repo.repositoryName().toLowerCase(Locale.ROOT).contains("resume")) {
            strengths.add("Project theme is practical and relevant for career-oriented platforms");
        }

        return distinct(strengths);
    }

    private List<String> buildWeaknesses(
            GitHubProjectAnalysisRequest request,
            List<String> technologies,
            int overallScore
    ) {
        List<String> weaknesses = new ArrayList<>();

        if (!hasText(request.getProjectDescription())) {
            weaknesses.add("Direct project description was not provided, so analysis relies on heuristics");
        }
        if (safeList(technologies).size() <= 2) {
            weaknesses.add("Repository context does not strongly indicate broad technical depth");
        }
        if (overallScore < 65) {
            weaknesses.add("Project may need clearer architecture explanation and better positioning");
        }
        if (!defaultBoolean(request.getIncludeReadmeAnalysis(), Boolean.TRUE)) {
            weaknesses.add("README-focused documentation analysis was disabled");
        }

        return distinct(weaknesses);
    }

    private List<String> buildRisks(
            GitHubProjectAnalysisRequest request,
            List<String> technologies
    ) {
        List<String> risks = new ArrayList<>();

        risks.add("Since repository contents were not fetched directly in this version, some inferences may not fully match the actual codebase");
        if (!hasText(request.getProjectDescription())) {
            risks.add("Without project description, interview positioning may become too generic");
        }
        if (safeList(technologies).isEmpty()) {
            risks.add("Technology stack is not strongly evident from available input");
        }

        return distinct(risks);
    }

    private List<String> buildImprovementSuggestions(
            List<String> technologies,
            int overallScore
    ) {
        List<String> suggestions = new ArrayList<>();

        suggestions.add("Ensure the repository README clearly explains purpose, architecture, setup, and results");
        suggestions.add("Be ready to explain your exact contribution, decisions, and challenges");
        suggestions.add("Add measurable outcomes or impact if this project is shown on a resume");

        if (containsTech(technologies, "Spring Boot") || containsTech(technologies, "React")) {
            suggestions.add("Prepare to explain component/service structure and API/data flow");
        }
        if (overallScore < 70) {
            suggestions.add("Improve project documentation and align the repository story with the target role");
        }

        return distinct(suggestions);
    }

    private List<String> buildInterviewTalkingPoints(
            List<String> technologies
    ) {
        List<String> points = new ArrayList<>();

        points.add("Explain the project purpose in one clear sentence");
        points.add("Describe the overall architecture and main modules");
        points.add("Explain one challenging problem and how you solved it");
        points.add("Discuss the stack: " + String.join(", ", safeTake(technologies, 6)));

        if (containsTech(technologies, "AWS S3")) {
            points.add("Explain how file storage/upload/download flow works");
        }
        if (containsTech(technologies, "AI") || containsTech(technologies, "LLM")) {
            points.add("Explain how AI integration fits into the system and what problem it solves");
        }
        if (containsTech(technologies, "WebSocket")) {
            points.add("Explain why real-time communication was needed and how it was implemented");
        }

        return distinct(points);
    }

    private List<String> buildSuggestedInterviewQuestions(
            List<String> technologies
    ) {
        List<String> questions = new ArrayList<>();

        questions.add("What problem does this project solve, and why did you build it?");
        questions.add("Can you explain the architecture of this repository?");
        questions.add("What was the most difficult technical challenge in this project?");
        questions.add("How would you improve this project if you had more time?");

        if (containsTech(technologies, "Spring Boot")) {
            questions.add("How did you structure your backend services, controllers, and persistence layer?");
        }
        if (containsTech(technologies, "React") || containsTech(technologies, "Next.js")) {
            questions.add("How is your frontend organized, and how does it communicate with the backend?");
        }
        if (containsTech(technologies, "AWS S3")) {
            questions.add("How do you manage file upload, storage, and retrieval using AWS S3?");
        }
        if (containsTech(technologies, "AI") || containsTech(technologies, "LLM")) {
            questions.add("How does the AI part of the system work, and what inputs/outputs does it use?");
        }

        return distinct(questions);
    }

    private List<String> buildKeyConceptsToExplain(
            List<String> technologies
    ) {
        List<String> concepts = new ArrayList<>();

        if (containsTech(technologies, "Spring Boot")) {
            concepts.add("Layered backend architecture");
            concepts.add("REST API design");
        }
        if (containsTech(technologies, "React") || containsTech(technologies, "Next.js")) {
            concepts.add("Frontend component structure");
            concepts.add("State and data flow");
        }
        if (containsTech(technologies, "AWS S3")) {
            concepts.add("Object storage workflow");
        }
        if (containsTech(technologies, "WebSocket")) {
            concepts.add("Real-time event flow");
        }
        if (containsTech(technologies, "AI") || containsTech(technologies, "LLM")) {
            concepts.add("AI integration workflow");
            concepts.add("Prompt/request-response flow");
        }
        if (containsTech(technologies, "MySQL") || containsTech(technologies, "PostgreSQL") || containsTech(technologies, "MongoDB")) {
            concepts.add("Database design and persistence");
        }

        if (concepts.isEmpty()) {
            concepts.add("Project architecture");
            concepts.add("Implementation decisions");
        }

        return distinct(concepts);
    }

    private List<String> buildMissingOrUnclearTechnologies(
            GitHubProjectAnalysisRequest request,
            List<String> technologies
    ) {
        List<String> missing = new ArrayList<>();
        for (String declared : safeList(request.getDeclaredTechnologies())) {
            if (!containsTech(technologies, declared)) {
                missing.add(declared);
            }
        }
        return distinct(missing);
    }

    private List<String> buildNotableFeatures(
            ParsedGitHubRepo repo,
            List<String> technologies
    ) {
        List<String> features = new ArrayList<>();

        if (repo.repositoryName().toLowerCase(Locale.ROOT).contains("resume")) {
            features.add("Resume-related workflow");
        }
        if (repo.repositoryName().toLowerCase(Locale.ROOT).contains("interview")) {
            features.add("Interview-related workflow");
        }
        if (containsTech(technologies, "AWS S3")) {
            features.add("Cloud-based file storage");
        }
        if (containsTech(technologies, "WebSocket")) {
            features.add("Real-time communication support");
        }
        if (containsTech(technologies, "AI") || containsTech(technologies, "LLM")) {
            features.add("AI-assisted analysis or interaction");
        }

        return distinct(features);
    }

    private List<String> buildArchitectureNotes(
            ParsedGitHubRepo repo,
            List<String> technologies
    ) {
        List<String> notes = new ArrayList<>();

        if (containsTech(technologies, "Spring Boot") && (containsTech(technologies, "React") || containsTech(technologies, "Next.js"))) {
            notes.add("Likely follows a frontend-backend separated architecture");
        }
        if (containsTech(technologies, "AI")) {
            notes.add("May benefit from a separate AI-engine or integration layer");
        }
        if (containsTech(technologies, "AWS S3")) {
            notes.add("Storage concerns appear separated through cloud object storage");
        }
        if (containsTech(technologies, "WebSocket")) {
            notes.add("Real-time updates likely require event-driven or broker-backed communication");
        }

        if (notes.isEmpty()) {
            notes.add("Architecture details are inferred heuristically and should be validated against the actual repository structure");
        }

        return distinct(notes);
    }

    private List<GitHubProjectAnalysisResponse.RepositoryFileInfo> buildRelevantFiles(
            List<String> technologies
    ) {
        List<GitHubProjectAnalysisResponse.RepositoryFileInfo> files = new ArrayList<>();

        files.add(fileInfo("README.md", "documentation", "Primary project overview and setup instructions", true,
                "This should describe the project, setup, and key features."));

        if (containsTech(technologies, "Spring Boot")) {
            files.add(fileInfo("pom.xml", "build", "Java dependency and build configuration", true,
                    "Useful for verifying backend stack and major dependencies."));
            files.add(fileInfo("src/main/resources/application.properties", "configuration", "Backend configuration", true,
                    "Useful for understanding environment, DB, AWS, and integration settings."));
        }

        if (containsTech(technologies, "React") || containsTech(technologies, "Next.js")) {
            files.add(fileInfo("package.json", "build", "Frontend dependency and script configuration", true,
                    "Useful for verifying frontend framework and tooling."));
        }

        if (containsTech(technologies, "Docker")) {
            files.add(fileInfo("Dockerfile", "deployment", "Container build definition", false,
                    "Useful for understanding deployment packaging."));
        }

        return files;
    }

    private GitHubProjectAnalysisResponse.RepositoryMetadata buildRepositoryMetadata(
            GitHubProjectAnalysisRequest request,
            List<String> technologies
    ) {
        GitHubProjectAnalysisResponse.RepositoryMetadata metadata =
                new GitHubProjectAnalysisResponse.RepositoryMetadata();

        metadata.setDefaultBranch(firstNonBlank(request.getBranchName(), "main"));
        metadata.setPrimaryLanguage(detectPrimaryLanguage(technologies));
        metadata.setStars(null);
        metadata.setForks(null);
        metadata.setOpenIssues(null);
        metadata.setWatchers(null);
        metadata.setIsPrivate(null);
        metadata.setIsFork(null);
        metadata.setLicenseName(null);
        metadata.setHomepageUrl(null);
        metadata.setCreatedAt(null);
        metadata.setUpdatedAt(null);
        metadata.setPushedAt(null);

        return metadata;
    }

    private List<String> buildWarnings(GitHubProjectAnalysisRequest request) {
        List<String> warnings = new ArrayList<>();

        warnings.add("This analysis currently uses repository URL parsing and contextual heuristics, not live GitHub API fetching.");
        if (!defaultBoolean(request.getIncludeReadmeAnalysis(), Boolean.TRUE)) {
            warnings.add("README analysis was disabled in the request.");
        }
        if (!defaultBoolean(request.getIncludeRepositoryMetadata(), Boolean.TRUE)) {
            warnings.add("Repository metadata analysis was disabled in the request.");
        }
        if (!defaultBoolean(request.getIncludeFileStructureAnalysis(), Boolean.TRUE)) {
            warnings.add("File structure analysis was disabled in the request.");
        }

        return warnings;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private String buildDetectionText(GitHubProjectAnalysisRequest request, ParsedGitHubRepo repo) {
        return String.join(" ",
                defaultString(request.getRepositoryUrl()),
                defaultString(request.getOwner()),
                defaultString(request.getRepositoryName()),
                defaultString(request.getProjectTitle()),
                defaultString(request.getProjectDescription()),
                defaultString(request.getTargetRole()),
                defaultString(request.getJobDescription()),
                defaultString(request.getBranchName()),
                defaultString(repo.owner()),
                defaultString(repo.repositoryName()),
                String.join(" ", safeList(request.getDeclaredTechnologies()))
        );
    }

    private void addDeclaredTechnologies(Set<String> detected, List<String> declaredTechnologies) {
        for (String tech : safeList(declaredTechnologies)) {
            if (hasText(tech)) {
                detected.add(tech.trim());
            }
        }
    }

    private void detectIfContains(String text, Set<String> detected, String token, String label) {
        if (text.contains(token.toLowerCase(Locale.ROOT))) {
            detected.add(label);
        }
    }

    private boolean containsTech(List<String> technologies, String target) {
        for (String tech : safeList(technologies)) {
            if (tech.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private String detectPrimaryLanguage(List<String> technologies) {
        if (containsTech(technologies, "Java")) return "Java";
        if (containsTech(technologies, "Python")) return "Python";
        if (containsTech(technologies, "TypeScript")) return "TypeScript";
        if (containsTech(technologies, "JavaScript")) return "JavaScript";
        return null;
    }

    private GitHubProjectAnalysisResponse.RepositoryFileInfo fileInfo(
            String path,
            String type,
            String purpose,
            boolean important,
            String summary
    ) {
        GitHubProjectAnalysisResponse.RepositoryFileInfo info =
                new GitHubProjectAnalysisResponse.RepositoryFileInfo();
        info.setPath(path);
        info.setType(type);
        info.setPurpose(purpose);
        info.setImportant(important);
        info.setSummary(summary);
        return info;
    }

    private String humanizeRepoName(String repositoryName) {
        if (!hasText(repositoryName)) {
            return null;
        }
        String result = repositoryName.replace("-", " ").replace("_", " ").trim();
        if (result.isEmpty()) {
            return repositoryName;
        }
        return Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

    private String toTopic(String value) {
        return value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private List<String> safeTake(List<String> values, int limit) {
        List<String> safe = safeList(values);
        if (safe.isEmpty()) {
            return List.of("general development concepts");
        }
        return safe.subList(0, Math.min(limit, safe.size()));
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private Boolean defaultBoolean(Boolean value, Boolean fallback) {
        return value != null ? value : fallback;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private List<String> distinct(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : safeList(values)) {
            if (!hasText(value)) {
                continue;
            }
            String trimmed = value.trim();
            if (!result.contains(trimmed)) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Small parsed-repo value object.
     */
    private record ParsedGitHubRepo(String owner, String repositoryName) {
    }
}