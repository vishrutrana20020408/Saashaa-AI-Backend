package backend.ai_interview.exception;

/**
 * Resume Parsing Exception
 *
 * Thrown when the backend fails to parse or extract content
 * from an uploaded resume file (PDF/DOC/DOCX).
 */
@SuppressWarnings("all")
public class ResumeParsingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResumeParsingException(String message) {
        super(message);
    }

    public ResumeParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}