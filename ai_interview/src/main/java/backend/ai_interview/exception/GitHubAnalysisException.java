package backend.ai_interview.exception;

/**
 * GitHubAnalysisException
 *
 * Custom exception for handling failures related to GitHub repository
 * analysis and integration.
 *
 * -------------------------------------------------------------------------
 * USED IN
 * -------------------------------------------------------------------------
 * - GitHubAnalysisService
 * - GitHubAnalysisController
 * - AI-based GitHub project analysis flow
 * - GitHub metadata / README / repository structure fetch operations
 *
 * -------------------------------------------------------------------------
 * TYPICAL CAUSES
 * -------------------------------------------------------------------------
 * - Invalid GitHub repository URL
 * - Repository not found
 * - GitHub API request failed
 * - README / metadata could not be fetched
 * - Analysis service returned invalid or empty result
 * - Rate limit exceeded
 * - Private repository access denied
 *
 * -------------------------------------------------------------------------
 * DESIGN NOTES
 * -------------------------------------------------------------------------
 * - Keeps GitHub-specific failures separate from generic API errors
 * - Helps GlobalExceptionHandler return cleaner, module-specific responses
 * - Can be extended later with error codes or retry/fallback metadata
 */
@SuppressWarnings("all")
public class GitHubAnalysisException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Optional repository URL involved in the failure.
     */
    private String repositoryUrl;

    /**
     * Optional repository owner.
     */
    private String owner;

    /**
     * Optional repository name.
     */
    private String repositoryName;

    /**
     * Optional operation being performed.
     * Example:
     * - VALIDATE_URL
     * - FETCH_METADATA
     * - FETCH_README
     * - ANALYZE_REPOSITORY
     * - GENERATE_INTERVIEW_INSIGHTS
     */
    private String operation;

    public GitHubAnalysisException(String message) {
        super(message);
    }

    public GitHubAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitHubAnalysisException(String message, String operation, String repositoryUrl) {
        super(message);
        this.operation = operation;
        this.repositoryUrl = repositoryUrl;
    }

    public GitHubAnalysisException(
            String message,
            String operation,
            String repositoryUrl,
            String owner,
            String repositoryName
    ) {
        super(message);
        this.operation = operation;
        this.repositoryUrl = repositoryUrl;
        this.owner = owner;
        this.repositoryName = repositoryName;
    }

    public GitHubAnalysisException(
            String message,
            Throwable cause,
            String operation,
            String repositoryUrl,
            String owner,
            String repositoryName
    ) {
        super(message, cause);
        this.operation = operation;
        this.repositoryUrl = repositoryUrl;
        this.owner = owner;
        this.repositoryName = repositoryName;
    }

    /**
     * Factory: invalid repository URL
     */
    public static GitHubAnalysisException invalidRepositoryUrl(String repositoryUrl) {
        return new GitHubAnalysisException(
                "Invalid GitHub repository URL: " + repositoryUrl,
                "VALIDATE_URL",
                repositoryUrl
        );
    }

    /**
     * Factory: repository not found
     */
    public static GitHubAnalysisException repositoryNotFound(String owner, String repositoryName) {
        String repoUrl = owner != null && repositoryName != null
                ? "https://github.com/" + owner + "/" + repositoryName
                : null;

        return new GitHubAnalysisException(
                "GitHub repository not found: " + owner + "/" + repositoryName,
                "FETCH_METADATA",
                repoUrl,
                owner,
                repositoryName
        );
    }

    /**
     * Factory: metadata fetch failed
     */
    public static GitHubAnalysisException metadataFetchFailed(String repositoryUrl, Throwable cause) {
        return new GitHubAnalysisException(
                "Failed to fetch GitHub repository metadata for: " + repositoryUrl,
                cause,
                "FETCH_METADATA",
                repositoryUrl,
                null,
                null
        );
    }

    /**
     * Factory: README fetch failed
     */
    public static GitHubAnalysisException readmeFetchFailed(String repositoryUrl, Throwable cause) {
        return new GitHubAnalysisException(
                "Failed to fetch GitHub README for: " + repositoryUrl,
                cause,
                "FETCH_README",
                repositoryUrl,
                null,
                null
        );
    }

    /**
     * Factory: repository analysis failed
     */
    public static GitHubAnalysisException analysisFailed(String repositoryUrl, Throwable cause) {
        return new GitHubAnalysisException(
                "GitHub project analysis failed for repository: " + repositoryUrl,
                cause,
                "ANALYZE_REPOSITORY",
                repositoryUrl,
                null,
                null
        );
    }

    /**
     * Factory: invalid analysis result
     */
    public static GitHubAnalysisException invalidAnalysisResult(String repositoryUrl) {
        return new GitHubAnalysisException(
                "Invalid or empty GitHub analysis result for repository: " + repositoryUrl,
                "ANALYZE_REPOSITORY",
                repositoryUrl
        );
    }

    /**
     * Factory: GitHub rate limit exceeded
     */
    public static GitHubAnalysisException rateLimitExceeded(String repositoryUrl, Throwable cause) {
        return new GitHubAnalysisException(
                "GitHub API rate limit exceeded while processing repository: " + repositoryUrl,
                cause,
                "FETCH_METADATA",
                repositoryUrl,
                null,
                null
        );
    }

    /**
     * Factory: private repository access denied
     */
    public static GitHubAnalysisException accessDenied(String repositoryUrl) {
        return new GitHubAnalysisException(
                "Access denied for GitHub repository: " + repositoryUrl,
                "FETCH_METADATA",
                repositoryUrl
        );
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getOperation() {
        return operation;
    }
}