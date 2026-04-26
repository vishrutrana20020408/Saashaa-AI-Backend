package backend.ai_interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Resume Scan Response DTO
 *
 * Used in the latest backend-integrated resume scan / upload flow.
 *
 * Frontend expects a flexible response shape such as:
 * {
 *   "success": true/false,
 *   "message": "...",
 *   "parsed": { ... } (optional),
 *   "score": 0-100 (optional)
 * }
 *
 * Used by:
 * - POST /api/user/resume/scan
 * - POST /api/user/resume/upload (compatibility-friendly response shape)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("all")
public class ResumeScanResponse {

    /**
     * Indicates success or failure of scan/upload processing.
     */
    private boolean success;

    /**
     * Message describing the result.
     */
    private String message;

    /**
     * Optional parsed output from resume (skills, summary, metadata, etc.).
     * Kept as Object for flexibility (Map/String/DTO).
     */
    private Object parsed;

    /**
     * Optional score returned by backend (0-100) or any scoring logic.
     */
    private Integer score;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getParsed() {
        return parsed;
    }

    public void setParsed(Object parsed) {
        this.parsed = parsed;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public ResumeScanResponse(boolean success, String message, Object parsed, Integer score) {
        this.success = success;
        this.message = message;
        this.parsed = parsed;
        this.score = score;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("data")
    public Object getData() {
        return parsed;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("data")
    public void setData(Object data) {
        this.parsed = data;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("payload")
    public Object getPayload() {
        return parsed;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("payload")
    public void setPayload(Object payload) {
        this.parsed = payload;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("result")
    public Object getResult() {
        return parsed;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("result")
    public void setResult(Object result) {
        this.parsed = result;
    }

    /**
     * Convenience factory for success response with message only.
     */
    public static ResumeScanResponse ok(String message) {
        return new ResumeScanResponse(true, message, null, null);
    }

    /**
     * Convenience factory for success response with parsed data and score.
     */
    public static ResumeScanResponse ok(String message, Object parsed, Integer score) {
        return new ResumeScanResponse(true, message, parsed, score);
    }

    /**
     * Convenience factory for failure response.
     */
    public static ResumeScanResponse fail(String message) {
        return new ResumeScanResponse(false, message, null, null);
    }
}