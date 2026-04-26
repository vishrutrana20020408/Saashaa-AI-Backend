package backend.ai_interview.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/speech")
@SuppressWarnings("all")
public class SpeechProxyController {

    private final String aiEngineBaseUrl;
    private final HttpClient httpClient;

    @Autowired
    public SpeechProxyController(@Qualifier("aiEngineBaseUrl") String aiEngineBaseUrl) {
        this(aiEngineBaseUrl, HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build());
    }

    // Package-private constructor for testing
    SpeechProxyController(String aiEngineBaseUrl, HttpClient httpClient) {
        this.aiEngineBaseUrl = aiEngineBaseUrl;
        this.httpClient = httpClient != null ? httpClient : HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @PostMapping("/synthesize")
    public ResponseEntity<?> synthesizeSpeech(
            @RequestParam("text") String text,
            @RequestParam(value = "voice", defaultValue = "female") String voice,
            @RequestParam(value = "language", defaultValue = "en-IN") String language,
            @RequestParam(value = "output_format", defaultValue = "mp3") String outputFormat
    ) {
        String encodedText = text != null ? URLEncoder.encode(text, StandardCharsets.UTF_8) : "";
        String encodedVoice = URLEncoder.encode(voice != null ? voice : "female", StandardCharsets.UTF_8);
        String encodedLanguage = URLEncoder.encode(language != null ? language : "en-IN", StandardCharsets.UTF_8);
        String encodedFormat = URLEncoder.encode(outputFormat != null ? outputFormat : "mp3", StandardCharsets.UTF_8);

        String formBody = "text=" + encodedText
                + "&voice=" + encodedVoice
                + "&language=" + encodedLanguage
                + "&output_format=" + encodedFormat;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiEngineBaseUrl + "/api/speech/synthesize"))
                    .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .header("Accept", MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(formBody))
                    .timeout(java.time.Duration.ofSeconds(30))
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] audioBytes = response.body();

            if (response.statusCode() != HttpStatus.OK.value()) {
                String errorText = audioBytes != null ? new String(audioBytes, StandardCharsets.UTF_8) : "Speech synthesis request failed.";
                // Log the error for debugging
                System.err.println("AI-Engine speech synthesis error [" + response.statusCode() + "]: " + errorText);
                
                // Return the error from AI-Engine as-is if it's a client error
                if (response.statusCode() >= 400 && response.statusCode() < 500) {
                    return ResponseEntity.status(response.statusCode())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("error", "Speech synthesis request failed", "details", errorText));
                }
                
                // For server errors or other issues, return a more generic message
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("error", "Speech synthesis service temporarily unavailable"));
            }

            if (audioBytes == null || audioBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("error", "AI engine returned empty audio from speech synthesis."));
            }

            HttpHeaders headers = new HttpHeaders();
            String contentType = determineContentType(outputFormat);
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(audioBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioBytes);
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            System.err.println("Error reaching AI-Engine speech service: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "Unable to reach the AI-engine speech service. Please ensure the AI-Engine is running and properly configured."));
        }
    }

    private String determineContentType(String outputFormat) {
        if (outputFormat == null) return "audio/mpeg";
        return switch (outputFormat.toLowerCase().trim()) {
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "mp3" -> "audio/mpeg";
            case "flac" -> "audio/flac";
            case "m4a" -> "audio/mp4";
            default -> "audio/mpeg";
        };
    }
}
