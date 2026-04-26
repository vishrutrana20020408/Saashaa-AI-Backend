package backend.ai_interview.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC configuration
 * ✅ Static resource handling
 * ✅ Uploads directory exposure (if you store scanned resumes or generated files)
 *
 * Notes for the new frontend:
 * - The resume is uploaded via multipart (FormData) to /api/user/resume/scan
 * - This config is NOT required for multipart uploads.
 * - Keep /uploads/** only if you want to serve stored files back to the UI.
 */
@Configuration
@SuppressWarnings("all")
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.storage.root:uploads}")
    private String storageRoot;

    @Value("${app.resume.storage.root:uploads/resumes}")
    private String resumeStorageRoot;

    @Value("${app.resume.storage.public-prefix:/uploads/resumes}")
    private String resumePublicPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 1) Static resources from classpath (normal Spring Boot behavior, but explicit is fine)
        registry
                .addResourceHandler(
                        "/static/**",
                        "/public/**",
                        "/resources/**",
                        "/webjars/**"
                )
                .addResourceLocations(
                        "classpath:/static/",
                        "classpath:/public/",
                        "classpath:/resources/",
                        "classpath:/META-INF/resources/",
                        "classpath:/META-INF/resources/webjars/"
                )
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic());

        // 2) Expose profile documents and other uploads
        // Example URL: http://localhost:8080/uploads/<filename>
        
        String uploadLocation = storageRoot.endsWith("/") ? storageRoot : storageRoot + "/";
        if (!uploadLocation.startsWith("file:")) {
            uploadLocation = "file:" + uploadLocation;
        }

        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation)
                .setCacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic());

        // 3) Expose resume storage separately if needed
        // This ensures that even if resume storage is moved outside of the main uploads folder,
        // it can still be served via its public prefix.
        
        String resumeLocation = resumeStorageRoot.endsWith("/") ? resumeStorageRoot : resumeStorageRoot + "/";
        if (!resumeLocation.startsWith("file:")) {
            resumeLocation = "file:" + resumeLocation;
        }

        String resumeHandler = resumePublicPrefix.endsWith("/") ? resumePublicPrefix + "**" : resumePublicPrefix + "/**";

        registry
                .addResourceHandler(resumeHandler)
                .addResourceLocations(resumeLocation)
                .setCacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic());
    }
}