package backend.ai_interview.dto.request;

import java.util.List;

/**
 * Admin Onboarding Request DTO
 *
 * Sent from the frontend during the onboarding flow for admins.
 */
@SuppressWarnings("all")
public class AdminOnboardingRequest {

    private String domain;
    private String subDomainMode;
    private String subDomainSingle;
    private List<String> subDomainMulti;
    private List<String> jobTitles;
    private String class10MarksheetUrl;
    private String class12MarksheetUrl;
    private String graduationMarksheetUrl;
    private String postGraduationMarksheetUrl;

    public AdminOnboardingRequest() {
    }

    public AdminOnboardingRequest(
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubDomainMode() {
        return subDomainMode;
    }

    public void setSubDomainMode(String subDomainMode) {
        this.subDomainMode = subDomainMode;
    }

    public String getSubDomainSingle() {
        return subDomainSingle;
    }

    public void setSubDomainSingle(String subDomainSingle) {
        this.subDomainSingle = subDomainSingle;
    }

    public List<String> getSubDomainMulti() {
        return subDomainMulti;
    }

    public void setSubDomainMulti(List<String> subDomainMulti) {
        this.subDomainMulti = subDomainMulti;
    }

    public List<String> getJobTitles() {
        return jobTitles;
    }

    public void setJobTitles(List<String> jobTitles) {
        this.jobTitles = jobTitles;
    }

    public String getClass10MarksheetUrl() {
        return class10MarksheetUrl;
    }

    public void setClass10MarksheetUrl(String class10MarksheetUrl) {
        this.class10MarksheetUrl = class10MarksheetUrl;
    }

    public String getClass12MarksheetUrl() {
        return class12MarksheetUrl;
    }

    public void setClass12MarksheetUrl(String class12MarksheetUrl) {
        this.class12MarksheetUrl = class12MarksheetUrl;
    }

    public String getGraduationMarksheetUrl() {
        return graduationMarksheetUrl;
    }

    public void setGraduationMarksheetUrl(String graduationMarksheetUrl) {
        this.graduationMarksheetUrl = graduationMarksheetUrl;
    }

    public String getPostGraduationMarksheetUrl() {
        return postGraduationMarksheetUrl;
    }

    public void setPostGraduationMarksheetUrl(String postGraduationMarksheetUrl) {
        this.postGraduationMarksheetUrl = postGraduationMarksheetUrl;
    }
}
