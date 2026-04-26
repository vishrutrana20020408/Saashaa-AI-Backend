package backend.ai_interview.controller;

import backend.ai_interview.dto.request.ResumeContentUpdateRequest;
import backend.ai_interview.dto.request.ResumeDuplicateCreateRequest;
import backend.ai_interview.dto.request.ResumeSectionUpdateRequest;
import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.dto.response.ResumeEditorResponse;
import backend.ai_interview.dto.response.ResumePreviewResponse;
import backend.ai_interview.dto.response.ResumeVersionResponse;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.service.resume.ResumeEditorService;
import backend.ai_interview.service.resume.ResumePreviewService;
import backend.ai_interview.service.resume.ResumeVersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Resume Version Controller
 *
 * Handles user resume-version operations.
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/resume/versions")
public class ResumeVersionController {

    private final ResumeVersionService resumeVersionService;
    private final ResumeEditorService resumeEditorService;
    private final ResumePreviewService resumePreviewService;

    public ResumeVersionController(
            ResumeVersionService resumeVersionService,
            ResumeEditorService resumeEditorService,
            ResumePreviewService resumePreviewService
    ) {
        this.resumeVersionService = resumeVersionService;
        this.resumeEditorService = resumeEditorService;
        this.resumePreviewService = resumePreviewService;
    }

    /**
     * Fetch single resume version details
     * GET /api/user/resume/version/{versionId}
     */
    @GetMapping("/{versionId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeVersionResponse> getVersion(
            @PathVariable Long versionId,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            ResumeVersionResponse response = resumeVersionService.getVersion(userId, versionId);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ResumeVersionResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResumeVersionResponse.fail("Failed to load resume version."));
        }
    }

    /**
     * Fetch all versions of a resume
     * GET /api/user/resume/version/resume/{resumeId}
     */
    @GetMapping("/resume/{resumeId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<List<ResumeVersionResponse>>> getVersionsByResume(
            @PathVariable Long resumeId,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            List<ResumeVersionResponse> versions =
                    resumeVersionService.getVersionsByResume(userId, resumeId);

            return ResponseEntity.ok(
                    ApiResponse.success("Resume versions fetched successfully", versions)
            );
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("Failed to load resume versions."));
        }
    }

    /**
     * Fetch resume editor data for one version
     * GET /api/user/resume/version/{versionId}/editor
     */
    @GetMapping("/{versionId}/editor")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeEditorResponse> getEditorData(
            @PathVariable Long versionId,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            ResumeEditorResponse response = resumeEditorService.getEditorData(userId, versionId);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ResumeEditorResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResumeEditorResponse.fail("Failed to load resume editor data."));
        }
    }

    /**
     * Update full resume editor content
     * PUT /api/user/resume/version/{versionId}/editor
     */
    @PutMapping("/{versionId}/editor")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeEditorResponse> updateEditorData(
            @PathVariable Long versionId,
            @RequestBody ResumeContentUpdateRequest request,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            ResumeEditorResponse response = resumeEditorService.updateEditorData(userId, versionId, request);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ResumeEditorResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResumeEditorResponse.fail("Failed to update resume content."));
        }
    }

    /**
     * Update one resume section
     * PUT /api/user/resume/version/{versionId}/section
     */
    @PutMapping("/{versionId}/section")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeEditorResponse> updateSection(
            @PathVariable Long versionId,
            @RequestBody ResumeSectionUpdateRequest request,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            ResumeEditorResponse response = resumeEditorService.updateSection(userId, versionId, request);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ResumeEditorResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResumeEditorResponse.fail("Failed to update resume section."));
        }
    }

    /**
     * Create a duplicate resume version
     * POST /api/user/resume/version/duplicate
     */
    @PostMapping("/duplicate")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumeVersionResponse> createDuplicate(
            @RequestBody ResumeDuplicateCreateRequest request,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            ResumeVersionResponse response = resumeVersionService.createDuplicate(userId, request);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ResumeVersionResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResumeVersionResponse.fail("Failed to create duplicate resume version."));
        }
    }

    /**
     * Preview a resume version
     * GET /api/user/resume/version/{versionId}/preview
     */
    @GetMapping("/{versionId}/preview")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResumePreviewResponse> preview(
            @PathVariable Long versionId,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            ResumePreviewResponse response = resumePreviewService.getPreview(userId, versionId);
            return ResponseEntity.ok(response);
        } catch (ApiException ex) {
            return ResponseEntity.badRequest()
                    .body(ResumePreviewResponse.fail(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(ResumePreviewResponse.fail("Failed to load resume preview."));
        }
    }

    /**
     * Health check
     * GET /api/user/resume/version/ping
     */
    @GetMapping("/ping")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(
                ApiResponse.success("Resume version module is working", "OK")
        );
    }
}