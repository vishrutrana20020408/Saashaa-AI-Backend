package backend.ai_interview.entity.enums;

/**
 * Job Application Status
 *
 * Represents the lifecycle of a job application.
 *
 * CREATED   -> Application record created
 * TAILORED  -> Resume tailored for the job
 * APPLIED   -> User submitted application to company
 * INTERVIEW -> Candidate shortlisted / interview stage
 * REJECTED  -> Application rejected
 * OFFER     -> Offer received
 */
@SuppressWarnings("all")
public enum JobApplicationStatus {

    CREATED,
    TAILORED,
    APPLIED,
    INTERVIEW,
    REJECTED,
    OFFER
}