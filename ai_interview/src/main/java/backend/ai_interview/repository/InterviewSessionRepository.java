package backend.ai_interview.repository;

import backend.ai_interview.entity.InterviewSession;
import backend.ai_interview.entity.enums.InterviewMode;
import backend.ai_interview.entity.enums.InterviewStatus;
import backend.ai_interview.entity.enums.InterviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * InterviewSessionRepository
 *
 * Repository for InterviewSession persistence and lookup.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - basic CRUD for interview sessions
 * - fetch sessions by user/admin
 * - fetch active/latest sessions
 * - filter by status/type/mode
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - Method names are chosen to support common dashboard and interview flows
 * - Custom JPQL/native queries can be added later if pagination/reporting grows
 */
@Repository
@SuppressWarnings("all")
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    /**
     * Fetch all sessions for a user ordered newest first.
     */
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Fetch all sessions for an admin ordered newest first.
     */
    List<InterviewSession> findByAdminIdOrderByCreatedAtDesc(Long adminId);

    /**
     * Fetch all sessions for a user with a specific status.
     */
    List<InterviewSession> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, InterviewStatus status);

    /**
     * Fetch all sessions for an admin with a specific status.
     */
    List<InterviewSession> findByAdminIdAndStatusOrderByCreatedAtDesc(Long adminId, InterviewStatus status);

    /**
     * Fetch all sessions for a user with a specific interview type.
     */
    List<InterviewSession> findByUserIdAndInterviewTypeOrderByCreatedAtDesc(Long userId, InterviewType interviewType);

    /**
     * Fetch all sessions for an admin with a specific interview type.
     */
    List<InterviewSession> findByAdminIdAndInterviewTypeOrderByCreatedAtDesc(Long adminId, InterviewType interviewType);

    /**
     * Fetch all sessions for a user with a specific mode.
     */
    List<InterviewSession> findByUserIdAndInterviewModeOrderByCreatedAtDesc(Long userId, InterviewMode interviewMode);

    /**
     * Fetch all sessions for an admin with a specific mode.
     */
    List<InterviewSession> findByAdminIdAndInterviewModeOrderByCreatedAtDesc(Long adminId, InterviewMode interviewMode);

    /**
     * Fetch latest session for a user.
     */
    Optional<InterviewSession> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Fetch latest session for an admin.
     */
    Optional<InterviewSession> findTopByAdminIdOrderByCreatedAtDesc(Long adminId);

    /**
     * Fetch latest active session for a user.
     */
    Optional<InterviewSession> findTopByUserIdAndStatusOrderByCreatedAtDesc(Long userId, InterviewStatus status);

    /**
     * Fetch latest active session for an admin.
     */
    Optional<InterviewSession> findTopByAdminIdAndStatusOrderByCreatedAtDesc(Long adminId, InterviewStatus status);

    /**
     * Find sessions linked to a resume.
     */
    List<InterviewSession> findByResumeIdOrderByCreatedAtDesc(Long resumeId);

    /**
     * Find sessions linked to a specific resume version.
     */
    List<InterviewSession> findByResumeVersionIdOrderByCreatedAtDesc(Long resumeVersionId);

    /**
     * Check whether a user has any session with given status.
     */
    boolean existsByUserIdAndStatus(Long userId, InterviewStatus status);

    /**
     * Check whether an admin has any session with given status.
     */
    boolean existsByAdminIdAndStatus(Long adminId, InterviewStatus status);

    /**
     * Count sessions for user by status.
     */
    long countByUserIdAndStatus(Long userId, InterviewStatus status);

    /**
     * Count sessions for admin by status.
     */
    long countByAdminIdAndStatus(Long adminId, InterviewStatus status);

    /**
     * Count sessions by type.
     */
    long countByInterviewType(InterviewType interviewType);

    /**
     * Count sessions by mode.
     */
    long countByInterviewMode(InterviewMode interviewMode);

    /**
     * Find all sessions by status ordered newest first.
     */
    List<InterviewSession> findByStatusOrderByCreatedAtDesc(InterviewStatus status);

    /**
     * Find all sessions by type and status ordered newest first.
     */
    List<InterviewSession> findByInterviewTypeAndStatusOrderByCreatedAtDesc(
            InterviewType interviewType,
            InterviewStatus status
    );

    /**
     * Find all sessions by mode and status ordered newest first.
     */
    List<InterviewSession> findByInterviewModeAndStatusOrderByCreatedAtDesc(
            InterviewMode interviewMode,
            InterviewStatus status
    );
}