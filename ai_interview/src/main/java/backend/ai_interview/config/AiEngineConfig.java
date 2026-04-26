package backend.ai_interview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * AiEngineConfig
 *
 * Central configuration for communication between the Spring Boot backend
 * and the external AI-engine service.
 *
 * Responsibilities:
 * - Reads AI-engine connection properties
 * - Exposes a reusable RestClient bean for outbound AI-engine API calls
 * - Keeps all AI-engine URL configuration centralized
 *
 * -------------------------------------------------------------------------
 * REQUIRED CONFIGURATION
 * -------------------------------------------------------------------------
 * Add these properties in application.properties later:
 *
 * ai.engine.base-url=http://localhost:8000
 * ai.engine.connect-timeout-ms=5000
 * ai.engine.read-timeout-ms=120000
 *
 * -------------------------------------------------------------------------
 * DEPLOYMENT NOTES
 * -------------------------------------------------------------------------
 * 1. During local development:
 *    - backend may run on localhost:8080
 *    - AI-engine may run on localhost:8000
 *
 * 2. During deployment:
 *    - replace localhost with the deployed AI-engine URL
 *    - use environment variables instead of hardcoding values
 *
 * 3. Frontend should NEVER call the AI-engine directly.
 *    Only the backend should communicate with the AI-engine.
 *
 * 4. If you later secure the AI-engine with API keys or service tokens,
 *    add those headers in the RestClient bean configuration or in the
 *    dedicated AI client classes.
 */
@Configuration
@SuppressWarnings("all")
public class AiEngineConfig {

    /**
     * Base URL of the AI-engine service.
     * Example:
     * http://localhost:8000
     * http://127.0.0.1:8000
     * https://your-ai-engine-domain.com
     */
	@Value("${ai.engine.base-url}")
    private String baseUrl;

    /**
     * Optional connect timeout in milliseconds.
     * Reserved for future extension if you switch to a custom HTTP client.
     */
    @Value("${ai.engine.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    /**
     * Optional read timeout in milliseconds.
     * Reserved for future extension if you switch to a custom HTTP client.
     */
    @Value("${ai.engine.read-timeout-ms:120000}")
    private int readTimeoutMs;

    /**
     * Exposes the base URL as a bean so service/integration classes
     * can inject it when needed.
     *
     * @return AI-engine base URL
     */
    @Bean(name = "aiEngineBaseUrl")
    public String aiEngineBaseUrl() {
        return baseUrl;
    }

    /**
     * Primary RestClient bean for AI-engine communication.
     *
     * This bean is intentionally simple and easy to maintain.
     * You can later enhance it with:
     * - default authorization headers
     * - custom request interceptors
     * - logging
     * - timeout-aware request factory
     *
     * @return configured RestClient
     */
    @Bean(name = "aiEngineRestClient")
    public RestClient aiEngineRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Optional getter bean for connect timeout.
     * Useful if downstream integration classes want to inspect config values.
     *
     * @return connect timeout in milliseconds
     */
    @Bean(name = "aiEngineConnectTimeoutMs")
    public Integer aiEngineConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    /**
     * Optional getter bean for read timeout.
     * Useful if downstream integration classes want to inspect config values.
     *
     * @return read timeout in milliseconds
     */
    @Bean(name = "aiEngineReadTimeoutMs")
    public Integer aiEngineReadTimeoutMs() {
        return readTimeoutMs;
    }
}