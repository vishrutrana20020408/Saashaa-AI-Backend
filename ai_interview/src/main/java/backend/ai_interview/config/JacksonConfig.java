package backend.ai_interview.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson Configuration
 *
 * Centralized JSON configuration for the entire application.
 *
 * This configuration ensures:
 * - Proper serialization of Java 8 Date/Time classes
 * - No timestamp formatting for LocalDateTime
 * - Ignore unknown properties from frontend requests
 * - Remove null values from JSON responses
 */
@Configuration
@SuppressWarnings("all")
public class JacksonConfig {

    /**
     * Configured ObjectMapper bean used across the application.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        /*
         * Register modules
         */
        objectMapper.registerModule(new JavaTimeModule());

        /*
         * Serialization configuration
         */
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        /*
         * Deserialization configuration
         */
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        /*
         * Do not include null fields in API responses
         */
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper;
    }
}