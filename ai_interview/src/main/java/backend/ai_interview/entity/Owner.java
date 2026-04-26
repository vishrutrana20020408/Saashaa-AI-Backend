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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "Owners",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "owner_id"),
                @UniqueConstraint(columnNames = "email_address"),
                @UniqueConstraint(columnNames = "share_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings("all")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "s_no", nullable = false)
    private Long sNo;

    @Column(name = "owner_id", nullable = false, updatable = false, length = 36, unique = true)
    private String ownerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "email_address", nullable = false)
    private String emailAddress;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "owner_created_date", nullable = false)
    private LocalDate ownerCreatedDate;

    @Column(name = "owner_created_time", nullable = false)
    private LocalTime ownerCreatedTime;

    @Column(name = "share_id", nullable = false, length = 36)
    private String shareId;

    @Column(name = "role", nullable = false)
    private String role;

    @PrePersist
    public void prePersist() {
        if (this.ownerId == null || this.ownerId.isBlank()) {
            this.ownerId = UUID.randomUUID().toString();
        }
        if (this.shareId == null || this.shareId.isBlank()) {
            this.shareId = UUID.randomUUID().toString();
        }
        if (this.ownerCreatedDate == null) {
            this.ownerCreatedDate = LocalDate.now();
        }
        if (this.ownerCreatedTime == null) {
            this.ownerCreatedTime = LocalTime.now().withNano(0);
        }
        if (this.role == null || this.role.isBlank()) {
            this.role = "OWNER";
        }
    }
}
