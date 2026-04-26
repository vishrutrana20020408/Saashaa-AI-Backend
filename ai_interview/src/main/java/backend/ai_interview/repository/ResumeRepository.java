package backend.ai_interview.repository;

import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Resume Repository
 *
 * Handles database operations for Resume entity
 * in the latest backend-integrated project structure.
 *
 * Relationships:
 * AppUser (1) -> (N) Resume
 */
@Repository
@SuppressWarnings("all")
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * Find resume by public resume code.
     */
    Optional<Resume> findByResumeCode(String resumeCode);

    /**
     * Check if resume exists by resume code.
     */
    boolean existsByResumeCode(String resumeCode);

    /**
     * Fetch all resumes of a user.
     */
    List<Resume> findByUser(AppUser user);

    /**
     * Fetch all resumes by userId (business/public ID).
     */
    List<Resume> findByUser_UserId(String userId);

    /**
     * Fetch resumes of a user by status.
     */
    List<Resume> findByUser_UserIdAndStatus(String userId, String status);

    /**
     * Fetch resumes of a user ordered by newest first.
     */
    List<Resume> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    /**
     * Fetch resumes of a user by status ordered by newest first.
     */
    List<Resume> findByUser_UserIdAndStatusOrderByCreatedAtDesc(String userId, String status);

    /**
     * Count resumes owned by a user.
     */
    long countByUser_UserId(String userId);
}