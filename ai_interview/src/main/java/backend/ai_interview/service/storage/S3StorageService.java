package backend.ai_interview.service.storage;

import backend.ai_interview.exception.StorageOperationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * S3StorageService
 *
 * Handles AWS S3 storage operations:
 * - upload files
 * - download files
 * - delete files
 * - check existence
 * - build access URL
 */
@Service
@SuppressWarnings("all")
@ConditionalOnExpression("${aws.s3.bucket:'' != ''}")
public class S3StorageService {

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Value("${aws.region:ap-south-1}")
    private String region;

    @Value("${aws.s3.bucket:}")
    private String bucketName;

    private S3Client s3Client;

    /**
     * Initialize S3 client after Spring loads properties.
     */
    @PostConstruct
    public void init() {
        try {
            if (isBlank(accessKey) || isBlank(secretKey) || isBlank(bucketName)) {
                s3Client = null;
                System.out.println("AWS S3 is not configured. Storage features will not work until credentials are set.");
                return;
            }

            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

        } catch (Exception ex) {
            throw new StorageOperationException("Failed to initialize AWS S3 client", ex);
        }
    }

    /**
     * Upload file using byte[].
     */
    public String uploadFile(byte[] data, String fileName, String contentType) {
        validateClient();

        try {
            String key = generateKey(fileName);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(data));
            return buildFileUrl(key);

        } catch (S3Exception ex) {
            throw new StorageOperationException("Failed to upload file to S3", ex);
        }
    }

    /**
     * Upload file using InputStream.
     */
    public String uploadFile(InputStream inputStream, long contentLength, String fileName, String contentType) {
        validateClient();

        try {
            String key = generateKey(fileName);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            return buildFileUrl(key);

        } catch (S3Exception ex) {
            throw new StorageOperationException("Failed to upload file stream to S3", ex);
        }
    }

    /**
     * Download file as byte[] from S3 by key.
     */
    public byte[] downloadFile(String key) {
        validateClient();

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
            return response.asByteArray();

        } catch (NoSuchKeyException ex) {
            throw new StorageOperationException("File not found in S3: " + key, ex);
        } catch (S3Exception ex) {
            throw new StorageOperationException("Failed to download file from S3", ex);
        }
    }

    /**
     * Delete file from S3 by key.
     */
    public void deleteFile(String key) {
        validateClient();

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);

        } catch (S3Exception ex) {
            throw new StorageOperationException("Failed to delete file from S3", ex);
        }
    }

    /**
     * Check whether file exists in S3.
     */
    public boolean exists(String key) {
        validateClient();

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(request);
            return true;

        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw new StorageOperationException("Failed to check file existence in S3", ex);
        }
    }

    /**
     * Returns a file URL.
     * Note:
     * This is NOT a true pre-signed URL.
     * It returns the direct S3 object URL.
     */
    public URL generatePresignedUrl(String key, int expiryMinutes) {
        validateClient();

        try {
            return new URL(buildFileUrl(key));
        } catch (MalformedURLException ex) {
            throw new StorageOperationException("Failed to generate file URL", ex);
        }
    }

    /**
     * Build public S3 file URL from key.
     */
    public String buildFileUrl(String key) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    /**
     * Extract S3 object key from stored URL.
     */
    public String extractKeyFromUrl(String fileUrl) {
        if (isBlank(fileUrl)) {
            return null;
        }

        try {
            String base = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
            if (fileUrl.startsWith(base)) {
                return fileUrl.substring(base.length());
            }
            return fileUrl;
        } catch (Exception ex) {
            throw new StorageOperationException("Failed to extract S3 key from URL", ex);
        }
    }

    /**
     * Generate unique S3 key for uploaded file.
     */
    private String generateKey(String fileName) {
        String safeName = isBlank(fileName) ? "file" : fileName.replaceAll("\\s+", "_");
        return "uploads/" + UUID.randomUUID() + "_" + safeName;
    }

    /**
     * Validate S3 client initialization.
     */
    private void validateClient() {
        if (s3Client == null) {
            throw new StorageOperationException(
                    "S3 client is not initialized. Configure aws.access-key, aws.secret-key, aws.region, and aws.s3.bucket."
            );
        }
    }

    /**
     * Utility blank check.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}