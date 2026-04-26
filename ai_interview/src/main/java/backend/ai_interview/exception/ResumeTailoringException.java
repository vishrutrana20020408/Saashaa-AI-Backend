package backend.ai_interview.exception;

/**
 * Resume Tailoring Exception
 *
 * Thrown when the system fails to generate, process,
 * or optimize a tailored resume for a specific job description.
 *
 * Possible causes:
 * - Job description parsing failure
 * - Tool/skill extraction failure
 * - AI/logic tailoring failure
 * - Resume duplicate creation failure
 */
@SuppressWarnings("all")
public class ResumeTailoringException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResumeTailoringException(String message) {
        super(message);
    }

    public ResumeTailoringException(String message, Throwable cause) {
        super(message, cause);
    }
}