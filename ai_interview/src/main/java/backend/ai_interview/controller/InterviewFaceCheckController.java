package backend.ai_interview.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.ai_interview.service.integration.ai.AiEngineClient;

@RestController
@RequestMapping("/api/interview")
@SuppressWarnings("all")
public class InterviewFaceCheckController {

    private final AiEngineClient aiEngineClient;

    public InterviewFaceCheckController(AiEngineClient aiEngineClient) {
        this.aiEngineClient = aiEngineClient;
    }

    @PostMapping("/face-check")
    public Map<String, Object> checkFace(@RequestBody Map<String, Object> requestPayload) {
        return aiEngineClient.postForMap("/api/interview/face-check", requestPayload, "FACE_CHECK");
    }
}
