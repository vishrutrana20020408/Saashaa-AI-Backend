package backend.ai_interview.controller;

import backend.ai_interview.dto.request.GitHubProjectAnalysisRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.GitHubProjectAnalysisResponse;
import backend.ai_interview.service.github.GitHubAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GitHubAnalysisController
 *
 * REST controller for GitHub repository / project analysis.
 *
 * Responsibilities:
 * - analyze a GitHub repository from a repository URL
 * - analyze README / project description / detected stack
 * - generate AI-based summary and interview-relevant insights
 * - extract likely skills, project strengths, risks, and talking points
 *
 * Endpoints:
 * - POST /api/github/analyze
 * - POST /api/github/analyze/resume-project
 * - GET  /api/github/health
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/github")
public class GitHubAnalysisController {

    private final GitHubAnalysisService gitHubAnalysisService;

    public GitHubAnalysisController(GitHubAnalysisService gitHubAnalysisService) {
        this.gitHubAnalysisService = gitHubAnalysisService;
    }

    /**
     * Analyze a GitHub repository/project.
     *
     * POST /api/github/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<GitHubProjectAnalysisResponse>> analyzeRepository(
            @Valid @RequestBody GitHubProjectAnalysisRequest request
    ) {
        GitHubProjectAnalysisResponse response = gitHubAnalysisService.analyzeProject(request);

        return ResponseEntity.ok(
                ApiResponse.success("GitHub project analyzed successfully", response)
        );
    }

    /**
     * Analyze a GitHub project specifically in the context of a resume project.
     *
     * POST /api/github/analyze/resume-project
     */
    @PostMapping("/analyze/resume-project")
    public ResponseEntity<ApiResponse<GitHubProjectAnalysisResponse>> analyzeResumeProjectRepository(
            @Valid @RequestBody GitHubProjectAnalysisRequest request
    ) {
        GitHubProjectAnalysisResponse response = gitHubAnalysisService.analyzeResumeProject(request);

        return ResponseEntity.ok(
                ApiResponse.success("Resume-linked GitHub project analyzed successfully", response)
        );
    }

    /**
     * Lightweight health endpoint for GitHub analysis module.
     *
     * GET /api/github/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("GitHub analysis service is available", "OK")
        );
    }
}