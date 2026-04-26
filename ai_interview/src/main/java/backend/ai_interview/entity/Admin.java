package backend.ai_interview.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "Admin",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email_address"),
                @UniqueConstraint(columnNames = "share_id"),
                @UniqueConstraint(columnNames = "admin_id")
        }
)
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    /**
     * 1) S.No (serial number)
     *
     * This is the database primary key for JPA. When the database does not
     * auto-generate it, we assign a unique value before persisting.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "s_no", nullable = false)
    private Long sNo;

    /**
     * 2) Admin_ID (business/public identity)
     */
    @Column(name = "admin_id", nullable = false, updatable = false, length = 36, unique = true)
    private String adminId;

    /**
     * 3) Name
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 4) Surname
     */
    @Column(name = "surname", nullable = false)
    private String surname;

    /**
     * 5) Email Address (validated in DTO, unique in DB)
     */
    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    /**
     * 6) Mobile Number (validated in DTO, 10 digits)
     */
    @Column(name = "mobile_number", nullable = false, length = 10)
    private String mobileNumber;

    /**
     * 7) Password (encrypted/hashed - BCrypt)
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 8) Admin Created Date (auto set)
     */
    @Column(name = "admin_created_date", nullable = false)
    private LocalDate adminCreatedDate;

    /**
     * 9) Admin Created Time (auto set)
     */
    @Column(name = "admin_created_time", nullable = false)
    private LocalTime adminCreatedTime;

    /**
     * 10) Share_ID (unique per admin, not same as user table share_id constraint)
     */
    @Column(name = "share_id", nullable = false, length = 36)
    private String shareId;

    /**
     * 11) role (default ADMIN)
     */
    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "onboarding_domain")
    private String onboardingDomain;

    @Column(name = "onboarding_subdomain_mode")
    private String onboardingSubDomainMode;

    @Column(name = "onboarding_subdomain_single")
    private String onboardingSubDomainSingle;

    @Lob
    @Column(name = "onboarding_subdomain_multi")
    private String onboardingSubDomainMulti;

    @Lob
    @Column(name = "onboarding_job_titles")
    private String onboardingJobTitles;

    @Builder.Default
    @Column(name = "onboarding_done", nullable = false)
    private boolean onboardingDone = false;

    // Documents
    @Column(name = "class_10_marksheet_url", length = 1000)
    private String class10MarksheetUrl;

    @Column(name = "class_12_marksheet_url", length = 1000)
    private String class12MarksheetUrl;

    @Column(name = "graduation_marksheet_url", length = 1000)
    private String graduationMarksheetUrl;

    @Column(name = "post_graduation_marksheet_url", length = 1000)
    private String postGraduationMarksheetUrl;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    public Long getSNo() {
        return this.sNo;
    }

    public String getAdminId() {
        return this.adminId;
    }

    public String getName() {
        return this.name;
    }

    public String getSurname() {
        return this.surname;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public String getMobileNumber() {
        return this.mobileNumber;
    }

    public String getPassword() {
        return this.password;
    }

    public String getShareId() {
        return this.shareId;
    }

    public String getRole() {
        return this.role;
    }

    public String getOnboardingDomain() {
        return this.onboardingDomain;
    }

    public void setOnboardingDomain(String onboardingDomain) {
        this.onboardingDomain = onboardingDomain;
    }

    public String getOnboardingSubDomainMode() {
        return onboardingSubDomainMode;
    }

    public void setOnboardingSubDomainMode(String onboardingSubDomainMode) {
        this.onboardingSubDomainMode = onboardingSubDomainMode;
    }

    public String getOnboardingSubDomainSingle() {
        return onboardingSubDomainSingle;
    }

    public void setOnboardingSubDomainSingle(String onboardingSubDomainSingle) {
        this.onboardingSubDomainSingle = onboardingSubDomainSingle;
    }

    public String getOnboardingSubDomainMulti() {
        return onboardingSubDomainMulti;
    }

    public void setOnboardingSubDomainMulti(String onboardingSubDomainMulti) {
        this.onboardingSubDomainMulti = onboardingSubDomainMulti;
    }

    public String getOnboardingJobTitles() {
        return onboardingJobTitles;
    }

    public void setOnboardingJobTitles(String onboardingJobTitles) {
        this.onboardingJobTitles = onboardingJobTitles;
    }

    public boolean isOnboardingDone() {
        return onboardingDone;
    }

    public void setOnboardingDone(boolean onboardingDone) {
        this.onboardingDone = onboardingDone;
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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    /**
     * Auto-fill fields on insert
     */
    @PrePersist
    public void prePersist() {
        if (this.adminId == null) {
            this.adminId = UUID.randomUUID().toString();
        }
        if (this.shareId == null) {
            this.shareId = UUID.randomUUID().toString();
        }
        if (this.adminCreatedDate == null) {
            this.adminCreatedDate = LocalDate.now();
        }
        if (this.adminCreatedTime == null) {
            this.adminCreatedTime = LocalTime.now().withNano(0);
        }
        if (this.role == null || this.role.isBlank()) {
            this.role = "ADMIN";
        }
    }
}