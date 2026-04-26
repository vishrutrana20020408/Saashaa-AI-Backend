package backend.ai_interview.entity.enums;

/**
 * Resume Version Type
 *
 * Defines the type of a resume version.
 *
 * BASE      -> Original editable version created from uploaded resume
 * DUPLICATE -> Manual duplicate created by user
 * TAILORED  -> Resume tailored automatically for a job application
 */
@SuppressWarnings("all")
public enum ResumeVersionType {
    BASE,
    DUPLICATE,
    TAILORED
}