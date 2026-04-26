package backend.ai_interview;

import backend.ai_interview.service.integration.ai.AiEngineClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SystemConnectivityTest {

    @Autowired
    private AiEngineClient aiEngineClient;

    /**
     * Test if the AI Engine Client is correctly configured and can build payloads.
     */
    @Test
    public void testAiEngineClientConfiguration() {
        assertNotNull(aiEngineClient, "AiEngineClient should be injected");
        
        Map<String, Object> payload = aiEngineClient.payloadOf("key", "value");
        assertEquals("value", payload.get("key"));
    }

    /**
     * Test if the backend is ready to handle AI Engine responses.
     * This uses a real client but would normally require the AI engine to be running.
     * For CI/CD, we'd mock the RestClient.
     */
    @Test
    public void testAiEngineIntegrationLogic() {
        // This is a logic test, ensuring our client can handle map responses
        Map<String, Object> mockResponse = Map.of("success", true, "data", Map.of("question", "Tell me about yourself"));
        
        // We're testing the getString helper which is critical for connectivity
        String question = aiEngineClient.getString(mockResponse, "data");
        assertEquals("Tell me about yourself", question);
    }
}
