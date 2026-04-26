package backend.ai_interview.service.integration.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InterviewClientTest {

    private final AiEngineClient aiEngineClient = mock(AiEngineClient.class);
    private final InterviewClient interviewClient = new InterviewClient(aiEngineClient);

    @Test
    void generateNextQuestion_buildsPayloadAndMapsResponse() {
        Map<String, Object> aiResponse = new HashMap<>();
        aiResponse.put("question", "What is polymorphism?");
        aiResponse.put("question_type", "behavioral");
        aiResponse.put("difficulty", 2);
        aiResponse.put("questionIndex", 2);

        when(aiEngineClient.generateNextInterviewQuestion(any()))
                .thenReturn(aiResponse);

        InterviewClient.InterviewQuestionResult result = interviewClient.generateNextQuestion(
                123L,
                2,
                "technical",
                "Software Engineer",
                "engineering",
                "resume text",
                "job desc",
                List.of("What is OOP?"),
                List.of(Map.of("question", "What is OOP?", "answer", "It is object oriented")),
                "en",
                3
        );

        assertNotNull(result);
        assertEquals("What is polymorphism?", result.getQuestion());
        assertEquals("behavioral", result.getQuestionType());
        assertEquals(2, result.getDifficulty());
        assertEquals(2, result.getQuestionIndex());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(aiEngineClient).generateNextInterviewQuestion(payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("Software Engineer", payload.get("target_role"));
        assertEquals("technical", payload.get("interview_type"));
        assertEquals("engineering", payload.get("domain"));
        assertEquals("resume text", payload.get("resume_text"));
        assertEquals("job desc", payload.get("job_description"));
        assertEquals(List.of("What is OOP?"), payload.get("previous_questions"));
        assertEquals(List.of(Map.of("question", "What is OOP?", "answer", "It is object oriented")), payload.get("history"));
        assertEquals("en", payload.get("language"));
        assertEquals(123L, payload.get("session_id"));
        assertEquals(2, payload.get("question_index"));
    }

    @Test
    void startInterview_buildsStartPayloadAndCallsAiEngine() {
        Map<String, Object> aiResponse = new HashMap<>();
        aiResponse.put("question", "Welcome to the interview.");

        when(aiEngineClient.startInterview(any()))
                .thenReturn(aiResponse);

        InterviewClient.InterviewStartResult result = interviewClient.startInterview(
                "technical",
                "live",
                "SWE",
                "engineering",
                3,
                5,
                "Job details",
                "Resume summary",
                List.of("https://github.com/example"),
                "en"
        );

        assertNotNull(result);
        assertEquals("Welcome to the interview.", result.getQuestion());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(aiEngineClient).startInterview(payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("SWE", payload.get("target_role"));
        assertEquals("technical", payload.get("interview_type"));
        assertEquals("medium", payload.get("difficulty"));
        assertEquals("Resume summary", payload.get("resume_text"));
        assertEquals("Job details", payload.get("job_description"));
        assertEquals(5, payload.get("question_count"));
        assertEquals(1, payload.get("question_index"));
        assertEquals(List.of("https://github.com/example"), payload.get("github_urls"));
    }
}
