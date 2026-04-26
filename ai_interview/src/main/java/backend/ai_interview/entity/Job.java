package backend.ai_interview.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@SuppressWarnings("all")
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("jobId")
    private Long id;

    @Column(name = "job_code", nullable = false, unique = true, length = 36)
    private String jobCode;

    @Column(name = "title", nullable = false)
    @Builder.Default
    private String title = "HR";

    @Column(name = "post", nullable = false)
    @Builder.Default
    private String post = "HR"; // Default "HR"

    @Column(name = "hr_type", nullable = false)
    private String hrType;

    @Column(name = "other_hr_type")
    private String otherHrType;

    @Column(name = "working_type", nullable = false)
    private String workingType; // WFH, OFFICE, HYBRID

    @Column(name = "office_location")
    private String officeLocation;

    @Column(name = "start_date_type", nullable = false)
    private String startDateType; // IMMEDIATE, SPECIFIC_DATE

    @Column(name = "specific_start_date")
    private java.time.LocalDate specificStartDate;

    @Column(nullable = false)
    private String salary;

    @Column(name = "last_date_to_apply", nullable = false)
    private java.time.LocalDate lastDateToApply;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "skills_required", columnDefinition = "TEXT")
    private String skillsRequired;

    @Column(name = "who_can_apply", columnDefinition = "TEXT")
    private String whoCanApply;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    @Builder.Default
    private String status = "OPEN"; // OPEN, CLOSED

    @Column(name = "domain", nullable = false)
    @Builder.Default
    private String domain = "TECH"; // TECH, NON_TECH

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.jobCode == null || this.jobCode.isBlank()) {
            this.jobCode = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "OPEN";
        }
        if (this.domain == null) {
            this.domain = "TECH";
        }
        if (this.post == null) {
            this.post = "HR";
        }
        if (this.title == null) {
            this.title = this.post;
        }
    }
}
