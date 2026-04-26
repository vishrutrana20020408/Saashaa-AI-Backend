package backend.ai_interview.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(
        name = "Company",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email_address"),
                @UniqueConstraint(columnNames = "share_id"),
                @UniqueConstraint(columnNames = "company_id")
        }
)
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "s_no", nullable = false)
    private Long sNo;

    @Column(name = "company_id", nullable = false, updatable = false, length = 36, unique = true)
    private String companyId;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_type", nullable = false)
    private String companyType;

    @Column(name = "contact_person_name", nullable = false)
    private String contactPersonName;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @Column(name = "mobile_number", nullable = false, length = 10)
    private String mobileNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "company_created_date", nullable = false)
    private LocalDate companyCreatedDate;

    @Column(name = "company_created_time", nullable = false)
    private LocalTime companyCreatedTime;

    @Column(name = "share_id", nullable = false, length = 36)
    private String shareId;

    @Column(name = "role", nullable = false)
    private String role;

    @PrePersist
    public void prePersist() {
        if (this.companyId == null) {
            this.companyId = UUID.randomUUID().toString();
        }
        if (this.shareId == null) {
            this.shareId = UUID.randomUUID().toString();
        }
        if (this.companyCreatedDate == null) {
            this.companyCreatedDate = LocalDate.now();
        }
        if (this.companyCreatedTime == null) {
            this.companyCreatedTime = LocalTime.now().withNano(0);
        }
        if (this.role == null || this.role.isBlank()) {
            this.role = "COMPANY";
        }
    }
}
