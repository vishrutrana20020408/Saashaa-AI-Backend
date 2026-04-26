package backend.ai_interview.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig
 *
 * Central WebSocket/STOMP configuration for real-time features such as:
 * - AI interview live session updates
 * - interview transcript streaming
 * - live scoring / feedback updates
 * - future notifications
 *
 * Frontend connects to:
 * - /ws-interview
 *
 * Frontend subscribes to:
 * - /topic/interview/{sessionId}
 * - /topic/interview/{sessionId}/transcript
 * - /topic/interview/{sessionId}/score
 * - /topic/interview/{sessionId}/feedback
 *
 * Frontend sends messages to:
 * - /app/interview/start
 * - /app/interview/answer
 * - /app/interview/next
 * - /app/interview/end
 */
@Configuration
@SuppressWarnings("all")
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Register STOMP endpoint(s) that frontend clients will connect to.
     */
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-interview")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000"
                )
                .withSockJS();
    }

    /**
     * Configure broker rules.
     */
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Optional inbound channel customization.
     */
    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        // Reserved for future interceptors
    }

    /**
     * Optional outbound channel customization.
     */
    @Override
    public void configureClientOutboundChannel(@NonNull ChannelRegistration registration) {
        // Reserved for future interceptors
    }
}