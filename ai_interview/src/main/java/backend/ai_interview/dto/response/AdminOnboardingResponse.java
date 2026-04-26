package backend.ai_interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Admin Onboarding Response DTO
 */
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminOnboardingResponse {

    private boolean success;
    private String message;
    private boolean done;
    private String domain;
    private String subDomainMode;
    private String subDomainSingle;
    private List<String> subDomainMulti;
    private List<String> jobTitles;
    private String class10MarksheetUrl;
    private String class12MarksheetUrl;
    private String graduationMarksheetUrl;
    private String postGraduationMarksheetUrl;

    public static AdminOnboardingResponse ok(String message) {
        return AdminOnboardingResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static AdminOnboardingResponse fail(String message) {
        return AdminOnboardingResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
