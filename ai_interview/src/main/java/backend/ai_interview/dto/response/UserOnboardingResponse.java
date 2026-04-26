package backend.ai_interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * User Onboarding Response DTO
 *
 * Used in the latest backend-integrated onboarding flow.
 *
 * Used by:
 * - POST /api/user/onboarding
 * - GET  /api/user/onboarding
 * - GET  /api/user/onboarding/status
 *
 * Frontend-friendly JSON:
 * {
 *   "success": true,
 *   "message": "Onboarding saved successfully",
 *   "domain": "Technical",
 *   "subDomainMode": "single",
 *   "subDomainSingle": "Infrastructure & DevOps",
 *   "subDomainMulti": [],
 *   "jobTitles": ["DevOps/Platform Engineering"]
 * }
 */
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserOnboardingResponse {

    private boolean success;
    private String message;

    private boolean done;

    private String domain;

    /**
     * "single" | "multi" | "any"
     */
    private String subDomainMode;

    /**
     * Used when subDomainMode = "single"
     */
    private String subDomainSingle;

    /**
     * Used when subDomainMode = "multi"
     */
    private List<String> subDomainMulti;

    /**
     * Optional list of job titles (multi-select).
     * Often empty if subDomainMode = "any" or "multi".
     */
    private List<String> jobTitles;

    private String class10MarksheetUrl;
    private String class12MarksheetUrl;
    private String graduationMarksheetUrl;
    private String postGraduationMarksheetUrl;

    /**
     * Convenience factory for success response.
     */
    public static UserOnboardingResponse ok(String message) {
        return UserOnboardingResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Convenience factory for failure response.
     */
    public static UserOnboardingResponse fail(String message) {
        return UserOnboardingResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}