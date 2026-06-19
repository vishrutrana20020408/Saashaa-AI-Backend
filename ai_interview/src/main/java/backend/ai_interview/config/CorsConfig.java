package backend.ai_interview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration
 *
 * ✅ Supports Next.js frontend (dev/prod) and multipart upload (resume scan)
 * ✅ Allows Authorization header for JWT
 * ✅ Allows FormData upload (no custom Content-Type needed)
 */
@Configuration
@SuppressWarnings("all")
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // ✅ Dev + optional LAN access (if you open frontend on phone)
                        .allowedOriginPatterns(
                                "http://localhost:3000",
                                "http://127.0.0.1:3000",
                                "http://192.168.*.*:3000",
                                "http://10.*.*.*:3000",
                                "http://172.16.*.*:3000",
                                "http://172.17.*.*:3000",
                                "http://172.18.*.*:3000",
                                "http://172.19.*.*:3000",
                                "http://172.20.*.*:3000",
                                "http://172.21.*.*:3000",
                                "http://172.22.*.*:3000",
                                "http://172.23.*.*:3000",
                                "http://172.24.*.*:3000",
                                "http://172.25.*.*:3000",
                                "http://172.26.*.*:3000",
                                "http://172.27.*.*:3000",
                                "http://172.28.*.*:3000",
                                "http://172.29.*.*:3000",
                                "http://172.30.*.*:3000",
                                "http://172.31.*.*:3000",
                                "https://*.vercel.app"
                        )

                        // ✅ Needed for onboarding + resume upload + all APIs
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                        // ✅ Must include Authorization (JWT) and accept multipart uploads
                        .allowedHeaders(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "Origin",
                                "X-Requested-With"
                        )

                        // ✅ If you ever return token in header
                        .exposedHeaders("Authorization")

                        // ✅ Cookies not used for JWT, but safe if you later add refresh cookies
                        .allowCredentials(true)

                        .maxAge(3600);
            }
        };
    }
}