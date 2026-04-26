package backend.ai_interview.repository;

import backend.ai_interview.entity.InterviewEvaluation;
import backend.ai_interview.entity.InterviewSession;
import backend.ai_interview.entity.InterviewTurn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * InterviewEvaluationRepository
 *
 * Repository for InterviewEvaluation persistence and lookup.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - basic CRUD for interview evaluations
 * - fetch turn-level and session-level evaluations
 * - fetch latest/final evaluation
 * - support re-evaluation history
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - A session may have many evaluations
 * - A turn may also have multiple evaluations (re-evaluation flow)
 * - Final evaluation may exist without a linked turn
 */
@Repository
@SuppressWarnings("all")
public interface InterviewEvaluationRepository extends JpaRepository<InterviewEvaluation, Long> {

    /**
     * Fetch all evaluations for a session ordered by creation time ascending.
     */
    List<InterviewEvaluation> findByInterviewSessionOrderByCreatedAtAsc(InterviewSession interviewSession);

    /**
     * Fetch all evaluations for a session id ordered by creation time ascending.
     */
    List<InterviewEvaluation> findByInterviewSessionIdOrderByCreatedAtAsc(Long interviewSessionId);

    /**
     * Fetch all evaluations for a session ordered newest first.
     */
    List<InterviewEvaluation> findByInterviewSessionOrderByCreatedAtDesc(InterviewSession interviewSession);

    /**
     * Fetch all evaluations for a session id ordered newest first.
     */
    List<InterviewEvaluation> findByInterviewSessionIdOrderByCreatedAtDesc(Long interviewSessionId);

    /**
     * Fetch all evaluations for a specific turn ordered by creation time ascending.
     */
    List<InterviewEvaluation> findByInterviewTurnOrderByCreatedAtAsc(InterviewTurn interviewTurn);

    /**
     * Fetch all evaluations for a specific turn id ordered by creation time ascending.
     */
    List<InterviewEvaluation> findByInterviewTurnIdOrderByCreatedAtAsc(Long interviewTurnId);

    /**
     * Fetch all evaluations for a specific turn ordered newest first.
     */
    List<InterviewEvaluation> findByInterviewTurnOrderByCreatedAtDesc(InterviewTurn interviewTurn);

    /**
     * Fetch all evaluations for a specific turn id ordered newest first.
     */
    List<InterviewEvaluation> findByInterviewTurnIdOrderByCreatedAtDesc(Long interviewTurnId);

    /**
     * Fetch latest evaluation for a session.
     */
    Optional<InterviewEvaluation> findTopByInterviewSessionOrderByCreatedAtDesc(InterviewSession interviewSession);

    /**
     * Fetch latest evaluation for a session id.
     */
    Optional<InterviewEvaluation> findTopByInterviewSessionIdOrderByCreatedAtDesc(Long interviewSessionId);

    /**
     * Fetch latest evaluation for a turn.
     */
    Optional<InterviewEvaluation> findTopByInterviewTurnOrderByCreatedAtDesc(InterviewTurn interviewTurn);

    /**
     * Fetch latest evaluation for a turn id.
     */
    Optional<InterviewEvaluation> findTopByInterviewTurnIdOrderByCreatedAtDesc(Long interviewTurnId);

    /**
     * Fetch latest evaluation of a specific type for a session.
     */
    Optional<InterviewEvaluation> findTopByInterviewSessionIdAndEvaluationTypeOrderByCreatedAtDesc(
            Long interviewSessionId,
            String evaluationType
    );

    /**
     * Fetch latest evaluation of a specific type for a turn.
     */
    Optional<InterviewEvaluation> findTopByInterviewTurnIdAndEvaluationTypeOrderByCreatedAtDesc(
            Long interviewTurnId,
            String evaluationType
    );

    /**
     * Fetch all evaluations of a specific type for a session.
     */
    List<InterviewEvaluation> findByInterviewSessionIdAndEvaluationTypeOrderByCreatedAtAsc(
            Long interviewSessionId,
            String evaluationType
    );

    /**
     * Fetch all evaluations of a specific type for a turn.
     */
    List<InterviewEvaluation> findByInterviewTurnIdAndEvaluationTypeOrderByCreatedAtAsc(
            Long interviewTurnId,
            String evaluationType
    );

    /**
     * Fetch all evaluations of a specific mode for a session.
     */
    List<InterviewEvaluation> findByInterviewSessionIdAndEvaluationModeOrderByCreatedAtAsc(
            Long interviewSessionId,
            String evaluationMode
    );

    /**
     * Fetch final session-level evaluations (turn is null).
     */
    List<InterviewEvaluation> findByInterviewSessionIdAndInterviewTurnIsNullOrderByCreatedAtAsc(Long interviewSessionId);

    /**
     * Fetch latest final session-level evaluation (turn is null).
     */
    Optional<InterviewEvaluation> findTopByInterviewSessionIdAndInterviewTurnIsNullOrderByCreatedAtDesc(
            Long interviewSessionId
    );

    /**
     * Fetch all forced re-evaluations for a session.
     */
    List<InterviewEvaluation> findByInterviewSessionIdAndForcedReevaluationTrueOrderByCreatedAtAsc(Long interviewSessionId);

    /**
     * Fetch all forced re-evaluations for a turn.
     */
    List<InterviewEvaluation> findByInterviewTurnIdAndForcedReevaluationTrueOrderByCreatedAtAsc(Long interviewTurnId);

    /**
     * Count all evaluations for a session.
     */
    long countByInterviewSessionId(Long interviewSessionId);

    /**
     * Count all evaluations for a turn.
     */
    long countByInterviewTurnId(Long interviewTurnId);

    /**
     * Count evaluations of a specific type for a session.
     */
    long countByInterviewSessionIdAndEvaluationType(Long interviewSessionId, String evaluationType);

    /**
     * Count evaluations of a specific type for a turn.
     */
    long countByInterviewTurnIdAndEvaluationType(Long interviewTurnId, String evaluationType);

    /**
     * Count forced re-evaluations for a session.
     */
    long countByInterviewSessionIdAndForcedReevaluationTrue(Long interviewSessionId);

    /**
     * Count forced re-evaluations for a turn.
     */
    long countByInterviewTurnIdAndForcedReevaluationTrue(Long interviewTurnId);

    /**
     * Check whether a session has at least one evaluation of a given type.
     */
    boolean existsByInterviewSessionIdAndEvaluationType(Long interviewSessionId, String evaluationType);

    /**
     * Check whether a turn has at least one evaluation of a given type.
     */
    boolean existsByInterviewTurnIdAndEvaluationType(Long interviewTurnId, String evaluationType);
}