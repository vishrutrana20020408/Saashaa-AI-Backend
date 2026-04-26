package backend.ai_interview.controller;

import backend.ai_interview.dto.request.ResumeTailorRequest;
import backend.ai_interview.dto.request.ToolKnowledgeAnswerRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.ResumeTailorResponse;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.service.resume.ResumeTailoringService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Resume Tailoring Controller
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/resume/tailor")
public class ResumeTailoringController {

    @SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ResumeTailoringController.class);

    private final ResumeTailoringService resumeTailoringService;

    public ResumeTailoringController(ResumeTailoringService resumeTailoringService) {
        this.resumeTailoringService = resumeTailoringService;
    }

    /**
     * Extract tools from JD
     */
    @PostMapping("/extract-tools")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeTailorResponse> extractTools(
            @RequestBody ResumeTailorRequest request,
            Authentication authentication
    ) {
        try {
            String userId = extractAuthenticatedUser(authentication);

            ResumeTailorResponse response =
                    resumeTailoringService.extractTools(userId, request);

            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ResumeTailorResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResumeTailorResponse.fail("Failed to extract tools."));
        }
    }

    /**
     * Apply resume tailoring
     */
    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeTailorResponse> tailorResume(
            @RequestBody ResumeTailorRequest request,
            Authentication authentication
    ) {
        try {
            String userId = extractAuthenticatedUser(authentication);

            ResumeTailorResponse response =
                    resumeTailoringService.tailorResume(userId, request);

            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ResumeTailorResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResumeTailorResponse.fail("Failed to tailor resume."));
        }
    }

    /**
     * Submit tool answers
     */
    @PostMapping("/tool-answers")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeTailorResponse> submitToolAnswers(
            @RequestBody ToolKnowledgeAnswerRequest request,
            Authentication authentication
    ) {
        try {
            String userId = extractAuthenticatedUser(authentication);

            ResumeTailorResponse response =
                    resumeTailoringService.submitToolAnswers(userId, request);

            return ResponseEntity.ok(response);

        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ResumeTailorResponse.fail(ex.getMessage()));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResumeTailorResponse.fail("Failed to submit tool answers."));
        }
    }

    /**
     * Health check
     */
    @GetMapping("/ping")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(
                ApiResponse.success("Resume tailoring module is working", "OK")
        );
    }

    /**
     * Extract authenticated user safely
     */
    private String extractAuthenticatedUser(Authentication authentication) {
        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new ApiException("Unauthorized user.");
        }
        return authentication.getName();
    }
}