package backend.ai_interview.entity.enums;

/**
 * Resume Status
 *
 * Represents the lifecycle state of a resume
 * in the latest backend-integrated project structure.
 *
 * Used across:
 * - root resume records
 * - resume management flows
 * - version-aligned resume lifecycle handling
 */
@SuppressWarnings("all")
public enum ResumeStatus {
    ACTIVE,
    ARCHIVED,
    DELETED
}