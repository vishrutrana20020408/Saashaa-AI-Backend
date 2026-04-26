package backend.ai_interview.service.integration.ai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import backend.ai_interview.exception.AiIntegrationException;

/**
 * AiEngineClient
 *
 * Central low-level client for communication between the Spring Boot backend
 * and the external AI-engine service.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - provide reusable POST/GET helpers
 * - hide HTTP communication details from higher-level services
 * - normalize AI-engine error handling
 * - provide simple typed/untyped request methods
 *
 * -------------------------------------------------------------------------
 * DESIGN NOTES
 * -------------------------------------------------------------------------
 * 1. This is intentionally a generic client.
 *    Feature-specific services can build on top of this, for example:
 *    - AtsClient
 *    - ResumeAnalysisClient
 *    - ResumeTailoringClient
 *    - InterviewClient
 *    - GitHubAnalysisClient
 *
 * 2. This client currently uses RestClient and Map-based payloads so it is
 *    easy to integrate even before all AI DTOs are finalized.
 *
 * 3. Later you can enhance this client with:
 *    - request interceptors
 *    - API-key headers
 *    - tracing / correlation ids
 *    - retry / circuit breaker support
 *    - better typed request/response DTOs
 *
 * -------------------------------------------------------------------------
 * EXPECTED AI-ENGINE ENDPOINT STYLE
 * -------------------------------------------------------------------------
 * Examples:
 * - POST /api/ats/score
 * - POST /api/resume/analyze
 * - POST /api/resume/tailor
 * - POST /api/interview/question
 * - POST /api/interview/feedback
 * - POST /api/interview/speech-turn
 * - POST /api/interview/score
 * - POST /api/github/analyze
 * - GET  /api/ai/health
 */
@Component
@SuppressWarnings("all")
public class AiEngineClient {

    private static final Logger log = LoggerFactory.getLogger(AiEngineClient.class);

    private final RestClient aiEngineRestClient;
    private final String aiEngineBaseUrl;
    private final Integer connectTimeoutMs;
    private final Integer readTimeoutMs;
    private final ObjectMapper objectMapper;

    public AiEngineClient(
            @Qualifier("aiEngineRestClient") RestClient aiEngineRestClient,
            @Qualifier("aiEngineBaseUrl") String aiEngineBaseUrl,
            @Qualifier("aiEngineConnectTimeoutMs") Integer connectTimeoutMs,
            @Qualifier("aiEngineReadTimeoutMs") Integer readTimeoutMs,
            ObjectMapper objectMapper
    ) {
        this.aiEngineRestClient = aiEngineRestClient;
        this.aiEngineBaseUrl = aiEngineBaseUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.objectMapper = objectMapper;
    }

    /**
     * Generic GET request returning a typed body.
     */
    public <T> T get(String path, Class<T> responseType, String operation) {
        try {
            T response = aiEngineRestClient.get()
                    .uri(normalizePath(path))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(responseType);

            if (response == null) {
                throw AiIntegrationException.invalidResponse(operation);
            }

            return response;
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw AiIntegrationException.requestFailed(operation, ex);
        }
    }

    /**
     * Generic POST request with typed request and typed response.
     */
    public <T, R> R post(String path, T requestBody, Class<R> responseType, String operation) {
        Object bodyValue = requestBody == null ? new LinkedHashMap<>() : requestBody;
        String requestJson = toJson(bodyValue);
        log.debug("AiEngineClient POST {} requestJson={}", path, requestJson);

        try {
            R response = aiEngineRestClient.post()
                    .uri(normalizePath(path))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(responseType);

            if (response == null) {
                throw AiIntegrationException.invalidResponse(operation);
            }

            return response;
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            String details = String.format("AI-engine request failed: status=%s, body=%s", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw AiIntegrationException.requestFailed(operation, new RuntimeException(details, ex));
        } catch (RuntimeException ex) {
            throw AiIntegrationException.requestFailed(operation, ex);
        }
    }

    /**
     * Generic POST request returning an untyped Map response.
     *
     * Useful during early integration when response DTOs are not finalized.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> postForMap(String path, Object requestBody, String operation) {
        Object bodyValue = requestBody == null ? new LinkedHashMap<>() : requestBody;
        String requestJson = toJson(bodyValue);
        log.debug("AiEngineClient POST {} requestJson={}", path, requestJson);

        try {
            Map<String, Object> response = aiEngineRestClient.post()
                    .uri(normalizePath(path))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(Map.class);

            response = unwrapMapResponse(response);
            if (response == null || response.isEmpty()) {
                throw AiIntegrationException.invalidResponse(operation);
            }

            return response;
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            String details = String.format("AI-engine request failed: status=%s, body=%s", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw AiIntegrationException.requestFailed(operation, new RuntimeException(details, ex));
        } catch (RuntimeException ex) {
            throw AiIntegrationException.requestFailed(operation, ex);
        }
    }

    /**
     * Generic GET request returning an untyped Map response.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getForMap(String path, String operation) {
        try {
            Map<String, Object> response = aiEngineRestClient.get()
                    .uri(normalizePath(path))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            response = unwrapMapResponse(response);
            if (response == null || response.isEmpty()) {
                throw AiIntegrationException.invalidResponse(operation);
            }

            return response;
        } catch (AiIntegrationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw AiIntegrationException.requestFailed(operation, ex);
        }
    }

    /**
     * Health check for AI-engine.
     */
    public Map<String, Object> checkHealth() {
        return getForMap("/api/ai/health", "HEALTH_CHECK");
    }

    /**
     * ATS scoring call.
     *
     * Suggested request payload:
     * {
     *   "resumeText": "...",
     *   "jobDescription": "...",
     *   "skills": [...]
     * }
     */
    public Map<String, Object> scoreAts(Map<String, Object> payload) {
        return postForMap("/api/ats/score", payload, "ATS_SCORING");
    }

    /**
     * Resume analysis call.
     */
    public Map<String, Object> analyzeResume(Map<String, Object> payload) {
        return postForMap("/api/resume/analyze", payload, "RESUME_ANALYSIS");
    }

    /**
     * Resume tool extraction call.
     */
    public Map<String, Object> extractResumeTools(Map<String, Object> payload) {
        return postForMap("/api/resume/extract-tools", payload, "TOOL_EXTRACTION");
    }

    /**
     * Resume tailoring call.
     */
    public Map<String, Object> tailorResume(Map<String, Object> payload) {
        return postForMap("/api/resume/tailor", payload, "RESUME_TAILORING");
    }

    /**
     * Interview start call.
     */
    public Map<String, Object> startInterview(Map<String, Object> payload) {
        log.debug("startInterview: payload={}", payload);
        return postForMap("/api/interview/question", payload, "QUESTION_GENERATION");
    }

    /**
     * Generate next interview question.
     */
    public Map<String, Object> generateNextInterviewQuestion(Map<String, Object> payload) {
        log.debug("generateNextInterviewQuestion: payload={}", payload);
        return postForMap("/api/interview/question", payload, "NEXT_QUESTION");
    }

    /**
     * Evaluate interview answer.
     */
    public Map<String, Object> evaluateInterviewAnswer(Map<String, Object> payload) {
        return postForMap("/api/interview/feedback", payload, "ANSWER_EVALUATION");
    }

    /**
     * Request mock-help or hint.
     */
    public Map<String, Object> generateInterviewHint(Map<String, Object> payload) {
        return postForMap("/api/interview/speech-turn", payload, "INTERVIEW_HINT");
    }

    /**
     * Generate final interview score.
     */
    public Map<String, Object> generateFinalInterviewScore(Map<String, Object> payload) {
        return postForMap("/api/interview/score", payload, "FINAL_SCORING");
    }

    /**
     * GitHub project analysis call.
     */
    public Map<String, Object> analyzeGitHubProject(Map<String, Object> payload) {
        return postForMap("/api/github/analyze", payload, "GITHUB_ANALYSIS");
    }

    /**
     * Helper to build a flexible payload map without needing DTOs immediately.
     */
    public Map<String, Object> payloadOf(Object... keyValuePairs) {
        if (keyValuePairs == null || keyValuePairs.length == 0) {
            return new LinkedHashMap<>();
        }
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Payload keyValuePairs must be even in length");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            Object key = keyValuePairs[i];
            Object value = keyValuePairs[i + 1];
            if (!(key instanceof String keyString) || keyString.isBlank()) {
                throw new IllegalArgumentException("Payload keys must be non-blank strings");
            }
            payload.put(keyString, value);
        }
        return payload;
    }

    /**
     * Helper to safely read a string from AI response map.
     */
    public String getString(Map<String, Object> response, String key) {
        if (response == null || key == null) {
            return null;
        }
        Object value = response.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue.trim();
        }
        if (value instanceof Map<?, ?> nestedMap) {
            Object nestedValue = nestedMap.get("text");
            if (nestedValue == null) {
                nestedValue = nestedMap.get("content");
            }
            if (nestedValue == null) {
                nestedValue = nestedMap.get("question");
            }
            if (nestedValue instanceof String nestedString) {
                return nestedString.trim();
            }
        }
        if (value instanceof List<?> listValue) {
            StringBuilder sb = new StringBuilder();
            for (Object item : listValue) {
                if (item != null) {
                    String part = String.valueOf(item).trim();
                    if (!part.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(part);
                    }
                }
            }
            return sb.length() == 0 ? null : sb.toString();
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    /**
     * Helper to safely read an integer from AI response map.
     */
    public Integer getInteger(Map<String, Object> response, String key) {
        if (response == null || key == null) {
            return null;
        }
        Object value = response.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue) {
            try {
                return Integer.valueOf(stringValue);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Helper to safely read a boolean from AI response map.
     */
    public Boolean getBoolean(Map<String, Object> response, String key) {
        if (response == null || key == null) {
            return null;
        }
        Object value = response.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        //noinspection Convert2PatternMatching
        if (value instanceof String stringValue) {
            return Boolean.valueOf(stringValue);
        }
        return null;
    }


    /**
     * Helper to safely read a nested map from AI response map.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(Map<String, Object> response, String key) {
        if (response == null || key == null) {
            return null;
        }
        Object value = response.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrapMapResponse(Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        if (response.size() == 1) {
            Object onlyValue = response.values().iterator().next();
            if (onlyValue instanceof Map<?, ?> nestedMap) {
                return (Map<String, Object>) nestedMap;
            }
        }
        if (response.containsKey("data") && response.get("data") instanceof Map<?, ?> dataMap) {
            return (Map<String, Object>) dataMap;
        }
        if (response.containsKey("result") && response.get("result") instanceof Map<?, ?> resultMap) {
            return (Map<String, Object>) resultMap;
        }
        return response;
    }

    /**
     * Helper to safely read a list from AI response map.
     */
    @SuppressWarnings("unchecked")
    public List<Object> getList(Map<String, Object> response, String key) {
        if (response == null || key == null) {
            return null;
        }
        Object value = response.get(key);
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        return null;
    }

    public String getAiEngineBaseUrl() {
        return aiEngineBaseUrl;
    }

    public Integer getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public Integer getReadTimeoutMs() {
        return readTimeoutMs;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("AI-engine path must not be blank");
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw AiIntegrationException.requestFailed("JSON_SERIALIZATION", ex);
        }
    }
}