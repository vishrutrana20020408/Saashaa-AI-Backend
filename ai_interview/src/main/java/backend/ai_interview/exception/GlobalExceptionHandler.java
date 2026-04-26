package backend.ai_interview.exception;

import backend.ai_interview.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.util.stream.Collectors;

/**
 * Global Exception Handler for the entire application
 *
 * Converts exceptions into consistent JSON responses.
 *
 * Latest project alignment:
 * - resume upload / scan flow
 * - resume version / editor / preview flow
 * - resume tailoring / AI-engine flow
 * - job application flow
 * - profile + resume-to-profile sync flow
 * - onboarding flow
 *
 * Response format stays consistent through ApiResponse:
 * {
 *   "success": false,
 *   "message": "..."
 * }
 */
@RestControllerAdvice
@SuppressWarnings("all")
public class GlobalExceptionHandler {

    /**
     * Handles custom API exceptions thrown by services/controllers
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<?>> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles profile not found errors
     */
    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleProfileNotFound(ProfileNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles profile sync related failures
     */
    @ExceptionHandler(ProfileSyncException.class)
    public ResponseEntity<ApiResponse<?>> handleProfileSync(ProfileSyncException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles resume not found errors
     */
    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResumeNotFound(ResumeNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles resume parsing failures
     */
    @ExceptionHandler(ResumeParsingException.class)
    public ResponseEntity<ApiResponse<?>> handleResumeParsing(ResumeParsingException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles resume storage failures
     */
    @ExceptionHandler(ResumeStorageException.class)
    public ResponseEntity<ApiResponse<?>> handleResumeStorage(ResumeStorageException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles resume editing failures
     */
    @ExceptionHandler(ResumeEditingException.class)
    public ResponseEntity<ApiResponse<?>> handleResumeEditing(ResumeEditingException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles resume tailoring failures
     */
    @ExceptionHandler(ResumeTailoringException.class)
    public ResponseEntity<ApiResponse<?>> handleResumeTailoring(ResumeTailoringException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles job application related failures
     */
    @ExceptionHandler(JobApplicationException.class)
    public ResponseEntity<ApiResponse<?>> handleJobApplication(JobApplicationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles file generation failures
     */
    @ExceptionHandler(FileGenerationException.class)
    public ResponseEntity<ApiResponse<?>> handleFileGeneration(FileGenerationException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * Handles validation errors triggered by @Valid on DTOs
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (message.isBlank()) {
            message = "Validation error";
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    /**
     * Handles invalid JSON / request parsing errors
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleBadJson(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("Invalid request body (JSON format error)"));
    }

    /**
     * Handles missing request parameters
     * Example: missing multipart field "file"
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = "Missing required parameter: " + ex.getParameterName();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    /**
     * Handles unsupported Content-Type
     * Useful when frontend sends wrong content type instead of multipart/form-data
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.fail("Unsupported Content-Type. Please upload using multipart/form-data."));
    }

    /**
     * Handles multipart parse errors (bad form-data)
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<?>> handleMultipart(MultipartException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("Invalid multipart request. Please upload the resume again."));
    }

    /**
     * Handles authentication errors (e.g. invalid JWT token)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("Authentication failed: " + ex.getMessage()));
    }

    /**
     * Handles authorization errors (e.g. wrong role)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("Access denied: You do not have permission to access this resource."));
    }

    /**
     * Fallback for any unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception ex) {
        // Log the exception in a real app
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("An unexpected server error occurred: " + ex.getMessage()));
    }


    /**
     * Handles file size exceeded errors
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.fail("File is too large. Please upload a smaller resume file."));
    }

    /**
     * Handles illegal arguments from service/controller logic
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        ex.getMessage() != null && !ex.getMessage().isBlank()
                                ? ex.getMessage()
                                : "Invalid request"
                ));
    }

    /**
     * Handles illegal state from service/controller logic
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        ex.getMessage() != null && !ex.getMessage().isBlank()
                                ? ex.getMessage()
                                : "Request cannot be processed"
                ));
    }
}