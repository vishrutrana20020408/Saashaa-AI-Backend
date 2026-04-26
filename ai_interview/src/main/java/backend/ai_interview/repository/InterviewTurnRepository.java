package backend.ai_interview.repository;

import backend.ai_interview.entity.InterviewSession;
import backend.ai_interview.entity.InterviewTurn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * InterviewTurnRepository
 *
 * Repository for InterviewTurn persistence and lookup.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - basic CRUD for interview turns
 * - fetch ordered turns for a session
 * - fetch latest/current turn
 * - fetch turns by evaluation/skip/hint state
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - Method names are designed for common interview flow operations
 * - Additional custom queries can be added later if analytics/reporting grows
 */
@Repository
@SuppressWarnings("all")
public interface InterviewTurnRepository extends JpaRepository<InterviewTurn, Long> {

    /**
     * Fetch all turns for a session ordered by question index ascending.
     */
    List<InterviewTurn> findByInterviewSessionOrderByQuestionIndexAsc(InterviewSession interviewSession);

    /**
     * Fetch all turns for a session id ordered by question index ascending.
     */
    List<InterviewTurn> findByInterviewSessionIdOrderByQuestionIndexAsc(Long interviewSessionId);

    /**
     * Fetch all turns for a session ordered by creation time ascending.
     */
    List<InterviewTurn> findByInterviewSessionOrderByCreatedAtAsc(InterviewSession interviewSession);

    /**
     * Fetch all turns for a session id ordered by creation time ascending.
     */
    List<InterviewTurn> findByInterviewSessionIdOrderByCreatedAtAsc(Long interviewSessionId);

    /**
     * Fetch latest turn for a session by question index.
     */
    Optional<InterviewTurn> findTopByInterviewSessionOrderByQuestionIndexDesc(InterviewSession interviewSession);

    /**
     * Fetch latest turn for a session id by question index.
     */
    Optional<InterviewTurn> findTopByInterviewSessionIdOrderByQuestionIndexDesc(Long interviewSessionId);

    /**
     * Fetch latest turn for a session by creation time.
     */
    Optional<InterviewTurn> findTopByInterviewSessionOrderByCreatedAtDesc(InterviewSession interviewSession);

    /**
     * Fetch latest turn for a session id by creation time.
     */
    Optional<InterviewTurn> findTopByInterviewSessionIdOrderByCreatedAtDesc(Long interviewSessionId);

    /**
     * Fetch a specific turn by session and question index.
     */
    Optional<InterviewTurn> findByInterviewSessionAndQuestionIndex(
            InterviewSession interviewSession,
            Integer questionIndex
    );

    /**
     * Fetch a specific turn by session id and question index.
     */
    Optional<InterviewTurn> findByInterviewSessionIdAndQuestionIndex(
            Long interviewSessionId,
            Integer questionIndex
    );

    /**
     * Fetch all evaluated turns for a session.
     */
    List<InterviewTurn> findByInterviewSessionIdAndEvaluatedTrueOrderByQuestionIndexAsc(Long interviewSessionId);

    /**
     * Fetch all non-evaluated turns for a session.
     */
    List<InterviewTurn> findByInterviewSessionIdAndEvaluatedFalseOrderByQuestionIndexAsc(Long interviewSessionId);

    /**
     * Fetch all skipped turns for a session.
     */
    List<InterviewTurn> findByInterviewSessionIdAndSkippedTrueOrderByQuestionIndexAsc(Long interviewSessionId);

    /**
     * Fetch all non-skipped turns for a session.
     */
    List<InterviewTurn> findByInterviewSessionIdAndSkippedFalseOrderByQuestionIndexAsc(Long interviewSessionId);

    /**
     * Fetch all turns where hint was used.
     */
    List<InterviewTurn> findByInterviewSessionIdAndHintUsedTrueOrderByQuestionIndexAsc(Long interviewSessionId);

    /**
     * Fetch all turns where sample answer was used.
     */
    List<InterviewTurn> findByInterviewSessionIdAndSampleAnswerUsedTrueOrderByQuestionIndexAsc(Long interviewSessionId);

    /**
     * Fetch all turns of a specific question type in a session.
     */
    List<InterviewTurn> findByInterviewSessionIdAndQuestionTypeOrderByQuestionIndexAsc(
            Long interviewSessionId,
            String questionType
    );

    /**
     * Count all turns for a session.
     */
    long countByInterviewSessionId(Long interviewSessionId);

    /**
     * Count evaluated turns for a session.
     */
    long countByInterviewSessionIdAndEvaluatedTrue(Long interviewSessionId);

    /**
     * Count skipped turns for a session.
     */
    long countByInterviewSessionIdAndSkippedTrue(Long interviewSessionId);

    /**
     * Count turns where hint was used for a session.
     */
    long countByInterviewSessionIdAndHintUsedTrue(Long interviewSessionId);

    /**
     * Check whether a turn exists for a session at given question index.
     */
    boolean existsByInterviewSessionIdAndQuestionIndex(Long interviewSessionId, Integer questionIndex);

    /**
     * Delete all turns for a specific session.
     */
    void deleteByInterviewSessionId(Long interviewSessionId);
}