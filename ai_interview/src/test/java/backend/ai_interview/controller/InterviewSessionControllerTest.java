package backend.ai_interview.controller;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

class InterviewSessionControllerTest {

    @Test
    void interviewSessionControllerHasLegacyAliasMapping() {
        RequestMapping requestMapping = InterviewSessionController.class.getAnnotation(RequestMapping.class);
        assertNotNull(requestMapping, "InterviewSessionController should be annotated with RequestMapping");
        String[] paths = requestMapping.value();
        assertArrayEquals(new String[]{"/api/user/interview/session", "/api/interview/session"}, paths);
    }

    @Test
    @SuppressWarnings("unchecked")
    void speechProxyControllerForwardsFormDataToAiEngine() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<byte[]> httpResponse = (HttpResponse<byte[]>) mock(HttpResponse.class);

        when(httpResponse.statusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.body()).thenReturn(new byte[]{1, 2, 3});
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        SpeechProxyController controller = new SpeechProxyController("http://localhost:9000", httpClient);
        ResponseEntity<?> result = controller.synthesizeSpeech("Hello world", "female", "en-IN", "mp3");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) result.getBody());
        assertEquals(MediaType.parseMediaType("audio/mpeg"), result.getHeaders().getContentType());
    }

    @Test
    @SuppressWarnings("unchecked")
    void speechProxyControllerReturnsJsonErrorOnAiEngineFailure() throws IOException, InterruptedException {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<byte[]> httpResponse = (HttpResponse<byte[]>) mock(HttpResponse.class);

        when(httpResponse.statusCode()).thenReturn(502);
        when(httpResponse.body()).thenReturn("service unavailable".getBytes(StandardCharsets.UTF_8));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        SpeechProxyController controller = new SpeechProxyController("http://localhost:9000", httpClient);
        ResponseEntity<?> result = controller.synthesizeSpeech("Hello world", "female", "en-IN", "mp3");

        assertEquals(HttpStatus.BAD_GATEWAY, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof Map);
        Map<String, Object> body = Objects.requireNonNull((Map<String, Object>) result.getBody());
        Object errorValue = body.get("error");
        assertNotNull(errorValue);
        assertEquals("AI-engine speech synthesis failed: service unavailable", errorValue);
        assertEquals(MediaType.APPLICATION_JSON, result.getHeaders().getContentType());
    }
}
