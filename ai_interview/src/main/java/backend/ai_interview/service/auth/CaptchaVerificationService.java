package backend.ai_interview.service.auth;

import backend.ai_interview.exception.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@SuppressWarnings("all")
public class CaptchaVerificationService {

    private final boolean enabled;
    private final String secret;
    private final String verifyUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CaptchaVerificationService(
            @Value("${app.captcha.enabled:false}") boolean enabled,
            @Value("${app.captcha.turnstile.secret:}") String secret,
            @Value("${app.captcha.turnstile.verify-url:https://challenges.cloudflare.com/turnstile/v0/siteverify}") String verifyUrl,
            ObjectMapper objectMapper
    ) {
        this.enabled = enabled;
        this.secret = secret != null ? secret.trim() : "";
        this.verifyUrl = verifyUrl != null ? verifyUrl.trim() : "";
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = objectMapper;
    }

    public void verify(String captchaToken) {
        if (!enabled) {
            return;
        }

        if (secret.isBlank()) {
            throw new IllegalStateException("CAPTCHA secret key is not configured. Set APP_CAPTCHA_ENABLED=false or provide TURNSTILE_SECRET.");
        }

        if (captchaToken == null || captchaToken.isBlank()) {
            throw new ApiException("CAPTCHA verification failed. Please complete the CAPTCHA.");
        }

        try {
            String requestBody = "secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8)
                    + "&response=" + URLEncoder.encode(captchaToken, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(verifyUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiException("CAPTCHA verification failed. Please try again later.");
            }

            Map<String, Object> responsePayload = objectMapper.readValue(
                    response.body(), new TypeReference<>() {
                    }
            );

            boolean success = Boolean.TRUE.equals(responsePayload.get("success"))
                    || "true".equalsIgnoreCase(String.valueOf(responsePayload.get("success")));

            if (!success) {
                String errors = String.valueOf(responsePayload.get("error-codes"));
                if (errors == null || errors.isBlank() || errors.equals("null")) {
                    errors = "Please complete the CAPTCHA and try again.";
                }
                throw new ApiException("CAPTCHA verification failed: " + errors);
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("CAPTCHA verification failed. Please try again.");
        }
    }
}
