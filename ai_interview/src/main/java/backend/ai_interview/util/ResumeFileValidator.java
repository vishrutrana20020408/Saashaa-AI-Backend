package backend.ai_interview.util;

import backend.ai_interview.exception.ApiException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

/**
 * ResumeFileValidator
 *
 * Validates resume upload inputs for:
 * - not null / not empty
 * - size limit
 * - allowed extensions: pdf, doc, docx
 * - best-effort Content-Type validation
 *
 * Used by:
 * - resume scan/upload flow
 * - resume storage/parsing pipeline
 *
 * Latest project update:
 * - supports resume preview/profile extraction pipeline
 * - validates file name more safely for stored resume assets
 * - exposes helper methods useful for resume storage/parsing/preview flow
 */
public final class ResumeFileValidator {

    private ResumeFileValidator() {
        // utility class
    }

    /**
     * Allowed extensions aligned with current resume upload flow.
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");

    /**
     * Common allowed content types (best-effort).
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * Validate the resume file.
     *
     * @param file     uploaded multipart file (field name: "file")
     * @param maxBytes maximum allowed bytes
     */
    public static void validate(MultipartFile file, long maxBytes) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Please upload a resume file.");
        }

        if (file.getSize() <= 0) {
            throw new ApiException("Resume file is empty.");
        }

        if (maxBytes > 0 && file.getSize() > maxBytes) {
            throw new ApiException("File is too large. Max allowed size is " + humanSize(maxBytes) + ".");
        }

        String filename = safeFilename(file);
        validateFilename(filename);

        String ext = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ApiException("Invalid resume format. Allowed: PDF, DOC, DOCX.");
        }

        String contentType = normalize(file.getContentType());
        if (!contentType.isBlank()) {
            boolean ok = ALLOWED_CONTENT_TYPES.contains(contentType)
                    || contentType.contains("officedocument")
                    || contentType.contains("msword")
                    || contentType.contains("wordprocessingml")
                    || contentType.contains("pdf");

            if (!ok) {
                throw new ApiException("Unsupported Content-Type for resume upload.");
            }
        }
    }

    /**
     * Validate and return normalized file extension.
     */
    public static String validateAndGetExtension(MultipartFile file, long maxBytes) {
        validate(file, maxBytes);
        return getFileExtension(safeFilename(file));
    }

    /**
     * Returns a sanitized storage-safe file name.
     */
    public static String getSanitizedFileName(MultipartFile file) {
        String filename = safeFilename(file);
        validateFilename(filename);

        String sanitized = filename
                .replace("\\", "_")
                .replace("/", "_")
                .replace("..", "_")
                .replaceAll("[\\r\\n\\t]+", "_")
                .replaceAll("[^a-zA-Z0-9._()\\-\\s]", "_")
                .trim()
                .replaceAll("\\s{2,}", " ");

        return sanitized.isBlank() ? "resume" : sanitized;
    }

    /**
     * Best-effort helper to identify whether uploaded file is preview-friendly.
     * PDF is directly preview-friendly; DOC/DOCX usually need conversion.
     */
    public static boolean isDirectPreviewSupported(MultipartFile file) {
        String extension = getFileExtension(safeFilename(file));
        return "pdf".equals(extension);
    }

    /**
     * Best-effort helper to identify Word document uploads.
     */
    public static boolean isWordDocument(MultipartFile file) {
        String extension = getFileExtension(safeFilename(file));
        return "doc".equals(extension) || "docx".equals(extension);
    }

    /**
     * Latest project update:
     * Helper to identify PDF uploads explicitly for parser/preview/storage flow.
     */
    public static boolean isPdfDocument(MultipartFile file) {
        String extension = getFileExtension(safeFilename(file));
        return "pdf".equals(extension);
    }

    /**
     * Latest project update:
     * Helper to validate a file name before external storage usage.
     */
    public static void validateFilenameOnly(String filename) {
        validateFilename(filename);
    }

    // ================= Helpers =================

    private static String safeFilename(MultipartFile file) {
        String n = file.getOriginalFilename();
        if (n == null) {
            return "resume";
        }
        n = n.trim();
        return n.isEmpty() ? "resume" : n;
    }

    private static void validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new ApiException("Invalid resume file name.");
        }

        if (filename.length() > 255) {
            throw new ApiException("Resume file name is too long.");
        }

        if (filename.contains("..")) {
            throw new ApiException("Invalid resume file name.");
        }
    }

    private static String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static String humanSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.ROOT, "%.0fKB", kb);
        }
        double mb = kb / 1024.0;
        return String.format(Locale.ROOT, "%.0fMB", mb);
    }
}