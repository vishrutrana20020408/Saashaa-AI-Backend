package backend.ai_interview.repository;

import backend.ai_interview.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Job Application Repository
 *
 * Handles database operations for JobApplication entity
 * in the latest backend-integrated project structure.
 *
 * Relationships:
 * AppUser (1) -> (N) JobApplication
 * ResumeVersion (1) -> (N) JobApplication
 */
@Repository
@SuppressWarnings("all")
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    /**
     * Find job application by public application code.
     */
    Optional<JobApplication> findByApplicationCode(String applicationCode);

    /**
     * Check if job application exists by application code.
     */
    boolean existsByApplicationCode(String applicationCode);

    /**
     * Fetch all job applications of a user.
     */
    List<JobApplication> findByUser_UserId(String userId);

    /**
     * Fetch all job applications of a user ordered by newest first.
     */
    List<JobApplication> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    /**
     * Fetch one job application only if it belongs to the user.
     */
    Optional<JobApplication> findByIdAndUser_UserId(Long id, String userId);

    /**
     * Fetch job applications by status.
     */
    List<JobApplication> findByStatus(String status);

    /**
     * Fetch job applications of a user by status.
     */
    List<JobApplication> findByUser_UserIdAndStatus(String userId, String status);

    /**
     * Fetch job applications by company name.
     */
    List<JobApplication> findByCompanyName(String companyName);

    /**
     * Fetch job applications of a user for a specific company.
     */
    List<JobApplication> findByUser_UserIdAndCompanyName(String userId, String companyName);

    /**
     * Fetch job applications of a user for a specific company ordered by newest first.
     */
    List<JobApplication> findByUser_UserIdAndCompanyNameOrderByCreatedAtDesc(String userId, String companyName);

    /**
     * Fetch job applications linked to a base resume version.
     */
    List<JobApplication> findByBaseResumeVersion_ResumeVersionId(Long baseResumeVersionId);

    /**
     * Fetch job applications linked to a tailored resume version.
     */
    List<JobApplication> findByTailoredResumeVersion_ResumeVersionId(Long tailoredResumeVersionId);

    /**
     * Count applications of a user.
     */
    long countByUser_UserId(String userId);

    /**
     * Count applications of a user by status.
     */
    long countByUser_UserIdAndStatus(String userId, String status);
}