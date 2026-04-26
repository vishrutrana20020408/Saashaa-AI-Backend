package backend.ai_interview.service;

import backend.ai_interview.dto.request.JobApplicationCreateRequest;
import backend.ai_interview.dto.request.ResumeTailorRequest;
import backend.ai_interview.dto.request.ToolKnowledgeAnswerRequest;
import backend.ai_interview.dto.response.JobApplicationResponse;
import backend.ai_interview.dto.response.ResumeTailorResponse;
import backend.ai_interview.exception.JobApplicationException;
import backend.ai_interview.service.resume.ResumeTailoringService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Job Application Service
 *
 * Handles:
 * - creating a job application
 * - fetching one job application
 * - fetching all job applications of a user
 *
 * NOTE:
 * This remains a clean starter service implementation.
 * It currently uses in-memory storage so controllers compile and work immediately.
 *
 * Later this can be replaced with:
 * - JobApplicationRepository
 * - ResumeVersionRepository
 * - ToolRequirementAnswerRepository
 * - ResumeTailoringService persistence logic
 *
 * Important business rule preserved:
 * - base/original resume is NOT modified
 * - application flow is assumed to create and use a tailored duplicate
 *
 * Latest project alignment:
 * - supports backend-integrated job application flow
 * - stays compatible with resume tailoring endpoints and DTOs
 * - keeps response payload aligned with version/preview-oriented resume flow
 */
@Service
@SuppressWarnings("all")
public class JobApplicationService {

    private final ResumeTailoringService resumeTailoringService;

    /**
     * Temporary in-memory store:
     * key   -> applicationId
     * value -> response DTO
     */
    private final Map<Long, JobApplicationResponse> applicationStore = new LinkedHashMap<>();

    /**
     * Tracks application ownership by userId.
     */
    private final Map<String, List<Long>> userApplications = new HashMap<>();

    private final AtomicLong applicationIdSequence = new AtomicLong(1L);

    public JobApplicationService(ResumeTailoringService resumeTailoringService) {
        this.resumeTailoringService = resumeTailoringService;
    }

    /**
     * Create a new job application for the authenticated user.
     */
    public JobApplicationResponse create(String userId, JobApplicationCreateRequest request) {
        validateUser(userId);
        validateCreateRequest(request);

        try {
            Long applicationId = applicationIdSequence.getAndIncrement();
            String applicationCode = generateApplicationCode(applicationId);

            ResumeTailorResponse tailoringResult = null;
            Integer atsBefore;
            Integer atsAfter;
            Long tailoredResumeVersionId = null;
            String tailoredResumeVersionCode = null;
            String status = "CREATED";

            if (request.shouldGenerateTailoredResume()) {
                ResumeTailorRequest tailorRequest = buildTailorRequest(request);
                tailoringResult = resumeTailoringService.tailorResume(userId, tailorRequest);

                atsBefore = tailoringResult.getAtsScoreBefore();
                atsAfter = tailoringResult.getAtsScoreAfter();
                tailoredResumeVersionId = buildTailoredVersionId(applicationId, request.getResumeVersionId());
                tailoredResumeVersionCode = generateTailoredVersionCode(tailoredResumeVersionId);
                status = "TAILORED";
            } else {
                atsBefore = estimateBaseAtsScore(request.getJobDescription());
                atsAfter = atsBefore;
            }

            List<Map<String, Object>> toolAnswers = mapToolAnswers(request.getToolAnswers());
            LocalDateTime now = LocalDateTime.now();

            JobApplicationResponse response = JobApplicationResponse.of(
                    applicationId,
                    applicationCode,
                    request.getResumeVersionId(),
                    tailoredResumeVersionId,
                    tailoredResumeVersionCode,
                    trimToNull(request.getCompanyName()),
                    trimToNull(request.getJobTitle()),
                    trimToNull(request.getApplicationSource()),
                    status,
                    atsBefore,
                    atsAfter,
                    toolAnswers,
                    now,
                    now
            );

            if (tailoringResult != null) {
                response.setMessage("Job application created and tailored resume generated successfully");
                response.setTailoredPreviewGenerated(Boolean.TRUE.equals(tailoringResult.getPreviewGenerated()));
                response.setDetectedTools(defaultList(tailoringResult.getDetectedTools()));
                response.setKeywords(defaultList(tailoringResult.getKeywords()));
            } else {
                response.setMessage("Job application created successfully");
                response.setTailoredPreviewGenerated(Boolean.FALSE);
            }

            applicationStore.put(applicationId, response);
            userApplications.computeIfAbsent(userId, key -> new ArrayList<>()).add(applicationId);

            return response;

        } catch (JobApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobApplicationException("Failed to create job application", ex);
        }
    }

    /**
     * Fetch one job application owned by the authenticated user.
     */
    public JobApplicationResponse getById(String userId, Long applicationId) {
        validateUser(userId);

        if (applicationId == null) {
            throw new JobApplicationException("Application id is required");
        }

        JobApplicationResponse response = applicationStore.get(applicationId);
        if (response == null) {
            throw new JobApplicationException("Job application not found");
        }

        List<Long> ownedApplications = userApplications.getOrDefault(userId, Collections.emptyList());
        if (!ownedApplications.contains(applicationId)) {
            throw new JobApplicationException("You are not allowed to access this job application");
        }

        return response;
    }

    /**
     * Fetch all job applications of the authenticated user.
     */
    public List<JobApplicationResponse> getAll(String userId) {
        validateUser(userId);

        List<Long> ids = userApplications.getOrDefault(userId, Collections.emptyList());

        return ids.stream()
                .map(applicationStore::get)
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    LocalDateTime t1 = a.getCreatedAt();
                    LocalDateTime t2 = b.getCreatedAt();
                    if (t1 == null && t2 == null) {
                        return 0;
                    }
                    if (t1 == null) {
                        return 1;
                    }
                    if (t2 == null) {
                        return -1;
                    }
                    return t2.compareTo(t1);
                })
                .collect(Collectors.toList());
    }

    private ResumeTailorRequest buildTailorRequest(JobApplicationCreateRequest request) {
        List<String> knownTools = new ArrayList<>();
        List<String> unknownTools = new ArrayList<>();

        if (request.getToolAnswers() != null) {
            for (ToolKnowledgeAnswerRequest answer : request.getToolAnswers()) {
                if (answer == null || isBlank(answer.getToolName())) {
                    continue;
                }

                if (answer.doesUserKnowTool()) {
                    knownTools.add(answer.getToolName().trim());
                } else {
                    unknownTools.add(answer.getToolName().trim());
                }
            }
        }

        return new ResumeTailorRequest(
                request.getResumeVersionId(),
                request.getCompanyName(),
                request.getJobTitle(),
                request.getJobDescription(),
                distinctList(knownTools),
                distinctList(unknownTools),
                request.getAdditionalNotes(),
                request.getGeneratePreview()
        );
    }

    private List<Map<String, Object>> mapToolAnswers(List<ToolKnowledgeAnswerRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (ToolKnowledgeAnswerRequest request : requests) {
            if (request == null) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("toolName", trimToNull(request.getToolName()));
            item.put("required", request.getRequired());
            item.put("userKnowsTool", request.getUserKnowsTool());
            item.put("userExperienceLevel", normalizeExperienceLevel(request));
            item.put("notes", trimToNull(request.getNotes()));
            item.put("decision", buildToolDecision(request));
            result.add(item);
        }

        return result;
    }

    private String buildToolDecision(ToolKnowledgeAnswerRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }
        if (request.doesUserKnowTool()) {
            return "INCLUDE_CONFIDENTLY";
        }
        if (request.isRequired()) {
            return "DO_NOT_CLAIM_REQUIRED_TOOL";
        }
        return "OPTIONAL_TOOL_NOT_INCLUDED";
    }

    private String normalizeExperienceLevel(ToolKnowledgeAnswerRequest request) {
        if (request == null) {
            return null;
        }

        String level = trimToNull(request.getUserExperienceLevel());
        if (level != null) {
            return level.toUpperCase(Locale.ROOT);
        }

        return request.doesUserKnowTool() ? "INTERMEDIATE" : "NONE";
    }

    private int estimateBaseAtsScore(String jobDescription) {
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            return 45;
        }

        int lengthFactor = Math.min(10, jobDescription.trim().length() / 500);
        int keywordFactor = Math.min(15, countPotentialKeywords(jobDescription));
        return Math.min(100, 45 + lengthFactor + keywordFactor);
    }

    private int countPotentialKeywords(String text) {
        String[] tokens = text.split("[^A-Za-z0-9+#./-]+");
        Set<String> unique = new HashSet<>();

        for (String token : tokens) {
            if (token != null && token.trim().length() >= 4) {
                unique.add(token.trim().toLowerCase(Locale.ROOT));
            }
        }

        return unique.size() / 8;
    }

    private Long buildTailoredVersionId(Long applicationId, Long baseVersionId) {
        long base = baseVersionId == null ? 0L : baseVersionId;
        return (applicationId * 1000L) + base;
    }

    private String generateApplicationCode(Long applicationId) {
        return String.format("APP-%05d", applicationId);
    }

    private String generateTailoredVersionCode(Long versionId) {
        return String.format("RV-TAILORED-%05d", versionId);
    }

    private List<String> distinctList(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }

        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private <T> List<T> defaultList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private void validateUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new JobApplicationException("Authenticated user is required");
        }
    }

    private void validateCreateRequest(JobApplicationCreateRequest request) {
        if (request == null) {
            throw new JobApplicationException("Job application request cannot be null");
        }
        if (request.getResumeVersionId() == null) {
            throw new JobApplicationException("Resume version id is required");
        }
        if (isBlank(request.getCompanyName())) {
            throw new JobApplicationException("Company name is required");
        }
        if (isBlank(request.getJobTitle())) {
            throw new JobApplicationException("Job title is required");
        }
        if (isBlank(request.getJobDescription())) {
            throw new JobApplicationException("Job description is required");
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