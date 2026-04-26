package backend.ai_interview.entity.enums;

/**
 * InterviewStatus
 *
 * Represents the lifecycle state of an interview session.
 *
 * -------------------------------------------------------------------------
 * FLOW
 * -------------------------------------------------------------------------
 * CREATED   -> session initialized but not started
 * ACTIVE    -> interview in progress
 * PAUSED    -> temporarily paused (optional future use)
 * COMPLETED -> interview finished successfully
 * CANCELLED -> user/system cancelled session
 * EXPIRED   -> session timed out or became inactive
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - Keep statuses generic so they work for both USER and ADMIN flows
 * - Additional states can be added later if needed
 */
@SuppressWarnings("all")
public enum InterviewStatus {

    /**
     * Session created but not yet started.
     */
    CREATED,

    /**
     * Interview is currently ongoing.
     */
    ACTIVE,

    /**
     * Interview temporarily paused (future extensibility).
     */
    PAUSED,

    /**
     * Interview completed normally.
     */
    COMPLETED,

    /**
     * Interview cancelled by user or system.
     */
    CANCELLED,

    /**
     * Interview expired due to inactivity or timeout.
     */
    EXPIRED
}