package backend.ai_interview.dto.request;

import java.util.List;

/**
 * User Onboarding Request DTO
 *
 * Sent from the frontend during the onboarding flow in the latest
 * backend-integrated project structure.
 *
 * This DTO stays aligned with:
 * - user onboarding persistence flow
 * - domain / subdomain selection logic
 * - frontend onboarding wizard updates
 *
 * Example JSON:
 *
 * {
 *   "domain": "Technical",
 *   "subDomainMode": "single",
 *   "subDomainSingle": "Infrastructure & DevOps",
 *   "subDomainMulti": [],
 *   "jobTitles": [
 *       "Cloud Computing/Architecture",
 *       "DevOps/Platform Engineering"
 *   ]
 * }
 */
@SuppressWarnings("all")
public class UserOnboardingRequest {

    /**
     * Domain selected by user.
     * Example:
     * - Technical
     * - Non-Technical
     */
    private String domain;

    /**
     * How subdomains are selected.
     *
     * Possible values:
     * - single -> user picked one
     * - multi  -> user picked multiple
     * - any    -> system decides the best track
     */
    private String subDomainMode;

    /**
     * Used when subDomainMode = single.
     */
    private String subDomainSingle;

    /**
     * Used when subDomainMode = multi.
     */
    private List<String> subDomainMulti;

    /**
     * Optional list of job titles.
     *
     * Typically not required when:
     * - subDomainMode = any
     * - subDomainMode = multi
     */
    private List<String> jobTitles;

    private String class10MarksheetUrl;
    private String class12MarksheetUrl;
    private String graduationMarksheetUrl;
    private String postGraduationMarksheetUrl;

    public UserOnboardingRequest() {
    }

    public UserOnboardingRequest(
            String domain,
            String subDomainMode,
            String subDomainSingle,
            List<String> subDomainMulti,
            List<String> jobTitles,
            String class10MarksheetUrl,
            String class12MarksheetUrl,
            String graduationMarksheetUrl,
            String postGraduationMarksheetUrl
    ) {
        this.domain = domain;
        this.subDomainMode = subDomainMode;
        this.subDomainSingle = subDomainSingle;
        this.subDomainMulti = subDomainMulti;
        this.jobTitles = jobTitles;
        this.class10MarksheetUrl = class10MarksheetUrl;
        this.class12MarksheetUrl = class12MarksheetUrl;
        this.graduationMarksheetUrl = graduationMarksheetUrl;
        this.postGraduationMarksheetUrl = postGraduationMarksheetUrl;
    }

    // ================= GETTERS =================

    public String getDomain() {
        return domain;
    }

    public String getSubDomainMode() {
        return subDomainMode;
    }

    public String getSubDomainSingle() {
        return subDomainSingle;
    }

    public List<String> getSubDomainMulti() {
        return subDomainMulti;
    }

    public List<String> getJobTitles() {
        return jobTitles;
    }

    public String getClass10MarksheetUrl() {
        return class10MarksheetUrl;
    }

    public String getClass12MarksheetUrl() {
        return class12MarksheetUrl;
    }

    public String getGraduationMarksheetUrl() {
        return graduationMarksheetUrl;
    }

    public String getPostGraduationMarksheetUrl() {
        return postGraduationMarksheetUrl;
    }

    // ================= SETTERS =================

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setSubDomainMode(String subDomainMode) {
        this.subDomainMode = subDomainMode;
    }

    public void setSubDomainSingle(String subDomainSingle) {
        this.subDomainSingle = subDomainSingle;
    }

    public void setSubDomainMulti(List<String> subDomainMulti) {
        this.subDomainMulti = subDomainMulti;
    }

    public void setJobTitles(List<String> jobTitles) {
        this.jobTitles = jobTitles;
    }

    public void setClass10MarksheetUrl(String class10MarksheetUrl) {
        this.class10MarksheetUrl = class10MarksheetUrl;
    }

    public void setClass12MarksheetUrl(String class12MarksheetUrl) {
        this.class12MarksheetUrl = class12MarksheetUrl;
    }

    public void setGraduationMarksheetUrl(String graduationMarksheetUrl) {
        this.graduationMarksheetUrl = graduationMarksheetUrl;
    }

    public void setPostGraduationMarksheetUrl(String postGraduationMarksheetUrl) {
        this.postGraduationMarksheetUrl = postGraduationMarksheetUrl;
    }

    @Override
    public String toString() {
        return "UserOnboardingRequest{" +
                "domain='" + domain + '\'' +
                ", subDomainMode='" + subDomainMode + '\'' +
                ", subDomainSingle='" + subDomainSingle + '\'' +
                ", subDomainMulti=" + subDomainMulti +
                ", jobTitles=" + jobTitles +
                ", class10MarksheetUrl='" + class10MarksheetUrl + '\'' +
                ", class12MarksheetUrl='" + class12MarksheetUrl + '\'' +
                ", graduationMarksheetUrl='" + graduationMarksheetUrl + '\'' +
                ", postGraduationMarksheetUrl='" + postGraduationMarksheetUrl + '\'' +
                '}';
    }
}