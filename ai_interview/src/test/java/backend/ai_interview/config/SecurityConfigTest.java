package backend.ai_interview.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import backend.ai_interview.controller.HealthController;
import backend.ai_interview.controller.HomeController;
import backend.ai_interview.security.JwtAuthFilter;
import backend.ai_interview.security.JwtService;

@WebMvcTest(controllers = {HomeController.class, HealthController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-12345678901234567890",
        "app.jwt.expiration-ms=3600000"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rootEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }
}
