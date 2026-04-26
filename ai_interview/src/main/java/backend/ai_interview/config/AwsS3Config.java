package backend.ai_interview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * AwsS3Config
 *
 * Central AWS S3 configuration for the backend.
 *
 * Responsibilities:
 * - Creates and exposes a reusable S3Client bean
 * - Reads AWS settings from application.properties or environment variables
 * - Supports both:
 *   1. Real AWS S3
 *   2. S3-compatible storage / local testing endpoint (optional)
 *
 * -------------------------------------------------------------------------
 * REQUIRED CONFIGURATION
 * -------------------------------------------------------------------------
 * Add these properties in application.properties later:
 *
 * aws.s3.region=ap-south-1
 * aws.s3.access-key=YOUR_ACCESS_KEY
 * aws.s3.secret-key=YOUR_SECRET_KEY
 * aws.s3.bucket-name=YOUR_BUCKET_NAME
 *
 * # Optional
 * aws.s3.endpoint=
 * aws.s3.path-style-access=false
 *
 * -------------------------------------------------------------------------
 * DEPLOYMENT NOTES
 * -------------------------------------------------------------------------
 * 1. For local development, you may keep placeholder values.
 * 2. For production deployment on Hostinger/Vercel/backend server:
 *    - move secrets to environment variables
 *    - do NOT hardcode credentials in source code
 * 3. Frontend should NEVER directly know AWS credentials.
 * 4. Backend should be the only layer talking to AWS S3.
 *
 * -------------------------------------------------------------------------
 * MAVEN DEPENDENCY NOTE
 * -------------------------------------------------------------------------
 * Make sure your pom.xml includes AWS SDK v2 S3 dependency:
 *
 * <dependency>
 *     <groupId>software.amazon.awssdk</groupId>
 *     <artifactId>s3</artifactId>
 * </dependency>
 *
 * If your project does not already use the AWS SDK BOM, you may also need
 * compatible version management in pom.xml.
 */
@Configuration
@SuppressWarnings("all")
@ConditionalOnExpression("${aws.s3.region:'' != ''}")
public class AwsS3Config {

    /**
     * AWS region for the S3 bucket.
     * Example: ap-south-1
     */
    @Value("${aws.s3.region:}")
    private String region;

    /**
     * AWS access key.
     * Keep this in environment variables for production.
     */
    @Value("${aws.s3.access-key:}")
    private String accessKey;

    /**
     * AWS secret key.
     * Keep this in environment variables for production.
     */
    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    /**
     * Optional custom endpoint.
     *
     * Leave empty for real AWS S3.
     * Use only if:
     * - local S3-compatible testing
     * - MinIO
     * - other compatible object storage
     */
    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    /**
     * Optional path-style access toggle.
     *
     * Usually false for AWS S3.
     * Can be true for local S3-compatible services such as MinIO.
     */
    @Value("${aws.s3.path-style-access:false}")
    private boolean pathStyleAccess;

    /**
     * Creates the main S3 client bean used across the backend.
     *
     * @return configured S3Client
     */
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));

        // Optional custom endpoint support for local development or S3-compatible services
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                    .forcePathStyle(pathStyleAccess);
        }

        return builder.build();
    }
}