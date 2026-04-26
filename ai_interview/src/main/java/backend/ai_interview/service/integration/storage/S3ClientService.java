package backend.ai_interview.service.integration.storage;

import backend.ai_interview.exception.StorageOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.Locale;
import java.util.UUID;

/**
 * S3ClientService
 *
 * Low-level AWS S3 integration service.
 *
 * -------------------------------------------------------------------------
 * RESPONSIBILITIES
 * -------------------------------------------------------------------------
 * - wraps direct AWS S3 SDK calls
 * - uploads files/bytes/streams
 * - downloads file bytes
 * - deletes objects
 * - checks object existence
 * - builds public object URLs
 *
 * -------------------------------------------------------------------------
 * DESIGN NOTES
 * -------------------------------------------------------------------------
 * 1. This is the low-level storage integration layer.
 * 2. Higher business services such as ResumeFileStorageService should call this.
 * 3. AWS credentials/client configuration should come from AwsS3Config.
 * 4. Do NOT place AWS secrets in this class.
 *
 * -------------------------------------------------------------------------
 * EXPECTED CONFIGURATION
 * -------------------------------------------------------------------------
 * application.properties:
 *
 * aws.s3.bucket-name=${AWS_S3_BUCKET_NAME:your-bucket-name}
 * aws.s3.region=${AWS_S3_REGION:ap-south-1}
 *
 * Optional:
 * aws.s3.endpoint=${AWS_S3_ENDPOINT:}
 *
 * -------------------------------------------------------------------------
 * REQUIRED BEAN
 * -------------------------------------------------------------------------
 * AwsS3Config should expose:
 * - S3Client s3Client()
 */
@Service
@SuppressWarnings("all")
@ConditionalOnExpression("${aws.s3.bucket-name:'' != ''}")
public class S3ClientService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:}")
    private String bucketName;

    @Value("${aws.s3.region:}")
    private String region;

    /**
     * Optional custom endpoint for S3-compatible services.
     * Leave blank for real AWS S3.
     */
    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    public S3ClientService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Upload bytes to S3 using a provided key.
     *
     * @param key storage key
     * @param bytes file content
     * @param contentType MIME type
     * @return uploaded key
     */
    public String uploadBytes(String key, byte[] bytes, String contentType) {
        validateKey(key);

        if (bytes == null || bytes.length == 0) {
            throw new StorageOperationException("Cannot upload empty file bytes");
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(resolveContentType(contentType, key))
                    .contentLength((long) bytes.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));
            return key;
        } catch (SdkException ex) {
            throw StorageOperationException.uploadFailed(key, ex);
        } catch (Exception ex) {
            throw new StorageOperationException(
                    "Unexpected error while uploading bytes to S3 for key: " + key,
                    ex,
                    "AWS_S3",
                    "UPLOAD",
                    key
            );
        }
    }

    /**
     * Upload stream to S3 using a provided key.
     *
     * @param key storage key
     * @param inputStream input stream
     * @param contentLength stream length
     * @param contentType MIME type
     * @return uploaded key
     */
    public String uploadStream(String key, InputStream inputStream, long contentLength, String contentType) {
        validateKey(key);

        if (inputStream == null) {
            throw new StorageOperationException("Input stream must not be null");
        }

        if (contentLength <= 0) {
            throw new StorageOperationException("Content length must be greater than zero");
        }

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(resolveContentType(contentType, key))
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            return key;
        } catch (SdkException ex) {
            throw StorageOperationException.uploadFailed(key, ex);
        } catch (Exception ex) {
            throw new StorageOperationException(
                    "Unexpected error while uploading stream to S3 for key: " + key,
                    ex,
                    "AWS_S3",
                    "UPLOAD",
                    key
            );
        }
    }

    /**
     * Download object bytes from S3.
     *
     * @param key storage key
     * @return downloaded bytes
     */
    public byte[] downloadBytes(String key) {
        validateKey(key);

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
            return response.asByteArray();
        } catch (NoSuchKeyException ex) {
            throw StorageOperationException.fileNotFound(key);
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                throw StorageOperationException.fileNotFound(key);
            }
            throw StorageOperationException.downloadFailed(key, ex);
        } catch (SdkException ex) {
            throw StorageOperationException.downloadFailed(key, ex);
        } catch (Exception ex) {
            throw new StorageOperationException(
                    "Unexpected error while downloading from S3 for key: " + key,
                    ex,
                    "AWS_S3",
                    "DOWNLOAD",
                    key
            );
        }
    }

    /**
     * Delete object from S3.
     *
     * @param key storage key
     */
    public void deleteObject(String key) {
        validateKey(key);

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
        } catch (SdkException ex) {
            throw StorageOperationException.deleteFailed(key, ex);
        } catch (Exception ex) {
            throw new StorageOperationException(
                    "Unexpected error while deleting from S3 for key: " + key,
                    ex,
                    "AWS_S3",
                    "DELETE",
                    key
            );
        }
    }

    /**
     * Check whether an object exists in S3.
     *
     * @param key storage key
     * @return true if exists
     */
    public boolean objectExists(String key) {
        validateKey(key);

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw new StorageOperationException(
                    "Failed to check object existence for key: " + key,
                    ex,
                    "AWS_S3",
                    "LOOKUP",
                    key
            );
        } catch (SdkException ex) {
            throw new StorageOperationException(
                    "Failed to check object existence for key: " + key,
                    ex,
                    "AWS_S3",
                    "LOOKUP",
                    key
            );
        }
    }

    /**
     * Return object metadata if available.
     *
     * @param key storage key
     * @return S3 object head response
     */
    public HeadObjectResponse getObjectMetadata(String key) {
        validateKey(key);

        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.headObject(request);
        } catch (NoSuchKeyException ex) {
            throw StorageOperationException.fileNotFound(key);
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                throw StorageOperationException.fileNotFound(key);
            }
            throw new StorageOperationException(
                    "Failed to fetch object metadata for key: " + key,
                    ex,
                    "AWS_S3",
                    "LOOKUP",
                    key
            );
        } catch (SdkException ex) {
            throw new StorageOperationException(
                    "Failed to fetch object metadata for key: " + key,
                    ex,
                    "AWS_S3",
                    "LOOKUP",
                    key
            );
        }
    }

    /**
     * Build the public URL for an object key.
     *
     * For private buckets, prefer backend download endpoints or presigned URLs later.
     *
     * @param key storage key
     * @return object URL
     */
    public String buildObjectUrl(String key) {
        validateKey(key);

        if (endpoint != null && !endpoint.isBlank()) {
            String normalizedEndpoint = endpoint.endsWith("/")
                    ? endpoint.substring(0, endpoint.length() - 1)
                    : endpoint;
            return normalizedEndpoint + "/" + bucketName + "/" + key;
        }

        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    /**
     * Build a unique key for uploaded objects under a folder prefix.
     *
     * Example output:
     * resumes/user-10/version-2/3f13f6b2_resume.pdf
     *
     * @param folderPrefix folder prefix
     * @param originalFileName original file name
     * @return generated unique key
     */
    public String buildUniqueKey(String folderPrefix, String originalFileName) {
        String safePrefix = normalizePrefix(folderPrefix);
        String safeFileName = sanitizeFileName(originalFileName);
        return safePrefix + UUID.randomUUID() + "_" + safeFileName;
    }

    /**
     * Extract S3 key from a full object URL when possible.
     *
     * @param fileUrl full URL or raw key
     * @return extracted key
     */
    public String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }

        String trimmed = fileUrl.trim();

        String directAwsPrefix = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
        if (trimmed.startsWith(directAwsPrefix)) {
            return trimmed.substring(directAwsPrefix.length());
        }

        if (endpoint != null && !endpoint.isBlank()) {
            String normalizedEndpoint = endpoint.endsWith("/")
                    ? endpoint.substring(0, endpoint.length() - 1)
                    : endpoint;
            String customPrefix = normalizedEndpoint + "/" + bucketName + "/";
            if (trimmed.startsWith(customPrefix)) {
                return trimmed.substring(customPrefix.length());
            }
        }

        return trimmed;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getRegion() {
        return region;
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new StorageOperationException("Storage key must not be blank");
        }
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }

        String normalized = prefix.trim().replace("\\", "/");
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file";
        }

        String normalized = fileName.trim().replace("\\", "/");
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }

        normalized = normalized
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_");

        return normalized.isBlank() ? "file" : normalized;
    }

    private String resolveContentType(String explicitContentType, String key) {
        if (explicitContentType != null && !explicitContentType.isBlank()) {
            return explicitContentType;
        }

        String guessed = URLConnection.guessContentTypeFromName(key);
        if (guessed != null && !guessed.isBlank()) {
            return guessed;
        }

        String lowerKey = key.toLowerCase(Locale.ROOT);
        if (lowerKey.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lowerKey.endsWith(".doc")) {
            return "application/msword";
        }
        if (lowerKey.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if (lowerKey.endsWith(".txt")) {
            return "text/plain";
        }
        if (lowerKey.endsWith(".png")) {
            return "image/png";
        }
        if (lowerKey.endsWith(".jpg") || lowerKey.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        return "application/octet-stream";
    }
}