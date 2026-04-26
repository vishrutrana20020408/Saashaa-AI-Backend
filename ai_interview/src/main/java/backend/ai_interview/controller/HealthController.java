package backend.ai_interview.controller;

import backend.ai_interview.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * HealthController
 *
 * System health and diagnostics controller.
 *
 * Responsibilities:
 * - Verify backend is running
 * - Provide lightweight system diagnostics
 * - Help frontend / DevOps confirm service availability
 * - Provide readiness info for integrations (AI-engine, AWS S3, etc.)
 *
 * -------------------------------------------------------------------------
 * ENDPOINTS
 * -------------------------------------------------------------------------
 * GET /api/health
 * GET /api/health/ping
 * GET /api/health/ready
 *
 * -------------------------------------------------------------------------
 * NOTES
 * -------------------------------------------------------------------------
 * - This controller does NOT expose sensitive information
 * - Safe for public usage in development
 * - You may restrict it in production if needed
 *
 * -------------------------------------------------------------------------
 * FUTURE EXTENSIONS
 * -------------------------------------------------------------------------
 * You can later add:
 * - database connectivity check
 * - AI-engine health check (via REST call)
 * - AWS S3 connectivity check
 * - memory / CPU diagnostics
 * - build version / git commit info
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Injected from application.properties (optional).
     * Example:
     * app.name=AI Interview Backend
     */
    @Value("${app.name:AI Interview Backend}")
    private String appName;

    /**
     * Injected from application.properties (optional).
     * Example:
     * app.version=1.0.0
     */
    @Value("${app.version:1.0.0}")
    private String appVersion;

    /**
     * Injected AI-engine base URL (optional, from AiEngineConfig).
     */
    @Value("${ai.engine.base-url:http://localhost:8000}")
    private String aiEngineBaseUrl;

    /**
     * Basic health check.
     *
     * GET /api/health
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", appName);
        data.put("version", appVersion);
        data.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(
                ApiResponse.success("Backend is healthy", data)
        );
    }

    /**
     * Lightweight ping endpoint.
     *
     * GET /api/health/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * Readiness check.
     *
     * Intended for:
     * - frontend startup checks
     * - deployment readiness probes
     * - system diagnostics
     *
     * NOTE:
     * This is a placeholder version.
     * You can later enhance it with:
     * - DB connectivity check
     * - AI-engine health check
     * - AWS S3 connectivity
     *
     * GET /api/health/ready
     */
    @GetMapping("/ready")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readiness() {

        Map<String, Object> checks = new HashMap<>();

        // Backend status
        checks.put("backend", "UP");

        // AI-engine (placeholder status)
        checks.put("aiEngineBaseUrl", aiEngineBaseUrl);
        checks.put("aiEngineStatus", "UNKNOWN"); // Future: call AI-engine /health

        // AWS S3 (placeholder status)
        checks.put("s3Status", "UNKNOWN"); // Future: add S3 connectivity check

        checks.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(
                ApiResponse.success("System readiness check completed", checks)
        );
    }
}