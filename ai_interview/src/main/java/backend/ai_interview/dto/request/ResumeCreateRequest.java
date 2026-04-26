package backend.ai_interview.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * ResumeCreateRequest
 *
 * Request DTO used when a user/admin creates a new resume directly from
 * the frontend resume editor instead of uploading an existing file.
 *
 * Typical usage:
 * - user clicks "Create Resume"
 * - frontend opens editor similar to a document editor
 * - frontend sends structured resume content to backend
 * - backend stores structured data
 * - backend generates PDF
 * - backend uploads generated file to AWS S3
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * 1. This DTO is for structured/manual resume creation.
 * 2. Uploaded file flow should use ResumeUploadRequest instead.
 * 3. Fields are intentionally flexible so future UI changes are easier.
 * 4. Empty sections are allowed unless specifically required by business rules.
 */
@SuppressWarnings("all")
public class ResumeCreateRequest {

    /**
     * Human-readable name for the resume.
     * Example:
     * - "Software Engineer Resume"
     * - "Backend Developer Resume 2026"
     */
    @NotBlank(message = "Resume name is required")
    @Size(max = 255, message = "Resume name must not exceed 255 characters")
    private String resumeName;

    /**
     * Optional title/heading shown near the top of the resume.
     * Example:
     * - "Java Backend Developer"
     * - "Full Stack Developer"
     */
    @Size(max = 255, message = "Resume title must not exceed 255 characters")
    private String resumeTitle;

    /**
     * Optional short professional summary/objective.
     */
    @Size(max = 5000, message = "Summary must not exceed 5000 characters")
    private String summary;

    /**
     * Contact details section.
     */
    @Valid
    private ContactInfo contact;

    /**
     * Skills section.
     */
    @Valid
    private List<SkillItem> skills = new ArrayList<>();

    /**
     * Education section.
     */
    @Valid
    private List<EducationItem> education = new ArrayList<>();

    /**
     * Experience / work history section.
     */
    @Valid
    private List<ExperienceItem> experience = new ArrayList<>();

    /**
     * Projects section.
     */
    @Valid
    private List<ProjectItem> projects = new ArrayList<>();

    /**
     * Certifications section.
     */
    @Valid
    private List<CertificationItem> certifications = new ArrayList<>();

    /**
     * Achievements / awards / extra information.
     */
    @Valid
    private List<String> achievements = new ArrayList<>();

    /**
     * Optional list of languages known by the candidate.
     */
    @Valid
    private List<String> languages = new ArrayList<>();

    /**
     * Optional links such as portfolio, LinkedIn, GitHub, LeetCode, etc.
     */
    @Valid
    private List<LinkItem> links = new ArrayList<>();

    /**
     * Optional flag for setting this resume as the user's base/default resume.
     */
    private Boolean baseVersion = Boolean.FALSE;

    /**
     * Optional notes from frontend/editor for future enhancements.
     * Not necessarily shown in final PDF.
     */
    @Size(max = 5000, message = "Internal notes must not exceed 5000 characters")
    private String notes;

    public ResumeCreateRequest() {
    }

    public String getResumeName() {
        return resumeName;
    }

    public void setResumeName(String resumeName) {
        this.resumeName = resumeName;
    }

    public String getResumeTitle() {
        return resumeTitle;
    }

    public void setResumeTitle(String resumeTitle) {
        this.resumeTitle = resumeTitle;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ContactInfo getContact() {
        return contact;
    }

    public void setContact(ContactInfo contact) {
        this.contact = contact;
    }

    public List<SkillItem> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillItem> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    public List<EducationItem> getEducation() {
        return education;
    }

    public void setEducation(List<EducationItem> education) {
        this.education = education != null ? education : new ArrayList<>();
    }

    public List<ExperienceItem> getExperience() {
        return experience;
    }

    public void setExperience(List<ExperienceItem> experience) {
        this.experience = experience != null ? experience : new ArrayList<>();
    }

    public List<ProjectItem> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectItem> projects) {
        this.projects = projects != null ? projects : new ArrayList<>();
    }

    public List<CertificationItem> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<CertificationItem> certifications) {
        this.certifications = certifications != null ? certifications : new ArrayList<>();
    }

    public List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<String> achievements) {
        this.achievements = achievements != null ? achievements : new ArrayList<>();
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages != null ? languages : new ArrayList<>();
    }

    public List<LinkItem> getLinks() {
        return links;
    }

    public void setLinks(List<LinkItem> links) {
        this.links = links != null ? links : new ArrayList<>();
    }

    public Boolean getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(Boolean baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Contact information block.
     */
    public static class ContactInfo {

        @Size(max = 255, message = "Full name must not exceed 255 characters")
        private String fullName;

        @Size(max = 255, message = "Email must not exceed 255 characters")
        private String email;

        @Size(max = 50, message = "Phone number must not exceed 50 characters")
        private String phone;

        @Size(max = 255, message = "Location must not exceed 255 characters")
        private String location;

        @Size(max = 500, message = "LinkedIn URL must not exceed 500 characters")
        private String linkedInUrl;

        @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
        private String githubUrl;

        @Size(max = 500, message = "Portfolio URL must not exceed 500 characters")
        private String portfolioUrl;

        public ContactInfo() {
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getLinkedInUrl() {
            return linkedInUrl;
        }

        public void setLinkedInUrl(String linkedInUrl) {
            this.linkedInUrl = linkedInUrl;
        }

        public String getGithubUrl() {
            return githubUrl;
        }

        public void setGithubUrl(String githubUrl) {
            this.githubUrl = githubUrl;
        }

        public String getPortfolioUrl() {
            return portfolioUrl;
        }

        public void setPortfolioUrl(String portfolioUrl) {
            this.portfolioUrl = portfolioUrl;
        }
    }

    /**
     * Skill item.
     */
    public static class SkillItem {

        @NotBlank(message = "Skill name is required")
        @Size(max = 255, message = "Skill name must not exceed 255 characters")
        private String name;

        @Size(max = 100, message = "Skill level must not exceed 100 characters")
        private String level;

        @Size(max = 100, message = "Skill category must not exceed 100 characters")
        private String category;

        public SkillItem() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    /**
     * Education item.
     */
    public static class EducationItem {

        @Size(max = 255, message = "Institution must not exceed 255 characters")
        private String institution;

        @Size(max = 255, message = "Degree must not exceed 255 characters")
        private String degree;

        @Size(max = 255, message = "Field of study must not exceed 255 characters")
        private String fieldOfStudy;

        @Size(max = 50, message = "Start date must not exceed 50 characters")
        private String startDate;

        @Size(max = 50, message = "End date must not exceed 50 characters")
        private String endDate;

        @Size(max = 100, message = "Grade must not exceed 100 characters")
        private String grade;

        @Size(max = 2000, message = "Education description must not exceed 2000 characters")
        private String description;

        public EducationItem() {
        }

        public String getInstitution() {
            return institution;
        }

        public void setInstitution(String institution) {
            this.institution = institution;
        }

        public String getDegree() {
            return degree;
        }

        public void setDegree(String degree) {
            this.degree = degree;
        }

        public String getFieldOfStudy() {
            return fieldOfStudy;
        }

        public void setFieldOfStudy(String fieldOfStudy) {
            this.fieldOfStudy = fieldOfStudy;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Work experience item.
     */
    public static class ExperienceItem {

        @Size(max = 255, message = "Company name must not exceed 255 characters")
        private String company;

        @Size(max = 255, message = "Role/title must not exceed 255 characters")
        private String title;

        @Size(max = 255, message = "Location must not exceed 255 characters")
        private String location;

        @Size(max = 50, message = "Start date must not exceed 50 characters")
        private String startDate;

        @Size(max = 50, message = "End date must not exceed 50 characters")
        private String endDate;

        private Boolean current = Boolean.FALSE;

        @Valid
        private List<String> responsibilities = new ArrayList<>();

        @Size(max = 4000, message = "Experience description must not exceed 4000 characters")
        private String description;

        public ExperienceItem() {
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public Boolean getCurrent() {
            return current;
        }

        public void setCurrent(Boolean current) {
            this.current = current;
        }

        public List<String> getResponsibilities() {
            return responsibilities;
        }

        public void setResponsibilities(List<String> responsibilities) {
            this.responsibilities = responsibilities != null ? responsibilities : new ArrayList<>();
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Project item.
     */
    public static class ProjectItem {

        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name must not exceed 255 characters")
        private String name;

        @Size(max = 5000, message = "Project description must not exceed 5000 characters")
        private String description;

        @Valid
        private List<String> technologies = new ArrayList<>();

        @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
        private String githubUrl;

        @Size(max = 500, message = "Live URL must not exceed 500 characters")
        private String liveUrl;

        @Size(max = 50, message = "Start date must not exceed 50 characters")
        private String startDate;

        @Size(max = 50, message = "End date must not exceed 50 characters")
        private String endDate;

        public ProjectItem() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getTechnologies() {
            return technologies;
        }

        public void setTechnologies(List<String> technologies) {
            this.technologies = technologies != null ? technologies : new ArrayList<>();
        }

        public String getGithubUrl() {
            return githubUrl;
        }

        public void setGithubUrl(String githubUrl) {
            this.githubUrl = githubUrl;
        }

        public String getLiveUrl() {
            return liveUrl;
        }

        public void setLiveUrl(String liveUrl) {
            this.liveUrl = liveUrl;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    /**
     * Certification item.
     */
    public static class CertificationItem {

        @Size(max = 255, message = "Certification name must not exceed 255 characters")
        private String name;

        @Size(max = 255, message = "Issuing organization must not exceed 255 characters")
        private String issuer;

        @Size(max = 50, message = "Issue date must not exceed 50 characters")
        private String issueDate;

        @Size(max = 50, message = "Expiry date must not exceed 50 characters")
        private String expiryDate;

        @Size(max = 255, message = "Credential ID must not exceed 255 characters")
        private String credentialId;

        @Size(max = 500, message = "Credential URL must not exceed 500 characters")
        private String credentialUrl;

        public CertificationItem() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getIssueDate() {
            return issueDate;
        }

        public void setIssueDate(String issueDate) {
            this.issueDate = issueDate;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
        }

        public String getCredentialId() {
            return credentialId;
        }

        public void setCredentialId(String credentialId) {
            this.credentialId = credentialId;
        }

        public String getCredentialUrl() {
            return credentialUrl;
        }

        public void setCredentialUrl(String credentialUrl) {
            this.credentialUrl = credentialUrl;
        }
    }

    /**
     * Generic link item.
     */
    public static class LinkItem {

        @NotBlank(message = "Link label is required")
        @Size(max = 100, message = "Link label must not exceed 100 characters")
        private String label;

        @NotBlank(message = "Link URL is required")
        @Size(max = 500, message = "Link URL must not exceed 500 characters")
        private String url;

        public LinkItem() {
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}