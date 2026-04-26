package backend.ai_interview.entity.enums;

/**
 * ProfileSourceType
 *
 * Represents how the official profile data was populated.
 *
 * Used for:
 * - tracking whether profile data came from resume parsing
 * - tracking manual edits after sync
 * - deciding profile sync/update behavior
 */
@SuppressWarnings("all")
public enum ProfileSourceType {

    /**
     * Profile created or updated manually by the user/admin
     */
    MANUAL,

    /**
     * Profile created or updated directly from resume parsing/sync
     */
    RESUME,

    /**
     * Profile contains both resume-synced and manually edited data
     */
    MIXED
}