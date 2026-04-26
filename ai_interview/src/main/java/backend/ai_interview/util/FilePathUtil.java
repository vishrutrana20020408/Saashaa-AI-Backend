package backend.ai_interview.util;

import backend.ai_interview.entity.Resume;
import backend.ai_interview.entity.ResumeVersion;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;

/**
 * File Path Utility
 *
 * Responsible for building consistent and safe file paths
 * for all resume related storage.
 *
 * Used by:
 * - ResumeStorageService
 * - FileGenerationService
 * - ResumePreviewService
 *
 * Storage design:
 *
 * uploads/
 *   resumes/
 *      {userId}/
 *          base/
 *              resume-original.pdf
 *          versions/
 *              {versionCode}/
 *                  resume.html
 *                  resume.txt
 *                  resume.pdf
 */
public final class FilePathUtil {

    private static final String ROOT_UPLOAD_DIR = "uploads";
    private static final String RESUME_DIR = "resumes";
    private static final String BASE_DIR = "base";
    private static final String VERSION_DIR = "versions";

    private FilePathUtil() {
        // Utility class
    }

    /**
     * Root uploads directory.
     */
    public static Path getUploadsRoot() {
        return Paths.get(ROOT_UPLOAD_DIR);
    }

    /**
     * Root resume directory.
     */
    public static Path getResumeRoot() {
        return getUploadsRoot().resolve(RESUME_DIR);
    }

    /**
     * User resume directory.
     */
    public static Path getUserResumeDir(String userId) {
        return getResumeRoot().resolve(safe(userId));
    }

    /**
     * Base resume directory.
     */
    public static Path getBaseResumeDir(String userId) {
        return getUserResumeDir(userId).resolve(BASE_DIR);
    }

    /**
     * Versions root directory.
     */
    public static Path getResumeVersionsDir(String userId) {
        return getUserResumeDir(userId).resolve(VERSION_DIR);
    }

    /**
     * Specific resume version directory.
     */
    public static Path getResumeVersionDir(String userId, String versionCode) {
        return getResumeVersionsDir(userId).resolve(safe(versionCode));
    }

    /**
     * Original uploaded resume path.
     */
    public static Path buildOriginalResumePath(String userId, String originalFileName) {
        return getBaseResumeDir(userId)
                .resolve(sanitizeFileName(originalFileName));
    }

    /**
     * HTML generated resume path.
     */
    public static Path buildHtmlResumePath(String userId, String versionCode) {
        return getResumeVersionDir(userId, versionCode)
                .resolve("resume.html");
    }

    /**
     * TXT generated resume path.
     */
    public static Path buildTextResumePath(String userId, String versionCode) {
        return getResumeVersionDir(userId, versionCode)
                .resolve("resume.txt");
    }

    /**
     * PDF generated resume path.
     */
    public static Path buildPdfResumePath(String userId, String versionCode) {
        return getResumeVersionDir(userId, versionCode)
                .resolve("resume.pdf");
    }

    /**
     * DOCX generated resume path.
     */
    public static Path buildDocxResumePath(String userId, String versionCode) {
        return getResumeVersionDir(userId, versionCode)
                .resolve("resume.docx");
    }

    /**
     * Build path for preview file.
     */
    public static Path buildPreviewPath(String userId, String versionCode) {
        return getResumeVersionDir(userId, versionCode)
                .resolve("preview.html");
    }

    /**
     * Build path for ATS report.
     */
    public static Path buildAtsReportPath(String userId, String versionCode) {
        return getResumeVersionDir(userId, versionCode)
                .resolve("ats-report.json");
    }

    /**
     * Build file path using Resume entity.
     */
    public static Path buildOriginalResumePath(Resume resume, String fileName) {
        return buildOriginalResumePath(
                resume.getUser().getUserId(),
                fileName
        );
    }

    /**
     * Build file path using ResumeVersion entity.
     */
    public static Path buildVersionHtmlPath(ResumeVersion version) {
        return buildHtmlResumePath(
                version.getResume().getUser().getUserId(),
                version.getVersionCode()
        );
    }

    public static Path buildVersionPdfPath(ResumeVersion version) {
        return buildPdfResumePath(
                version.getResume().getUser().getUserId(),
                version.getVersionCode()
        );
    }

    public static Path buildVersionTextPath(ResumeVersion version) {
        return buildTextResumePath(
                version.getResume().getUser().getUserId(),
                version.getVersionCode()
        );
    }

    /**
     * Build safe downloadable filename.
     */
    public static String buildDownloadName(ResumeVersion version, String extension) {

        String baseName = safe(version.getVersionName());
        if (baseName.isBlank()) {
            baseName = "resume";
        }

        baseName = baseName
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (baseName.isBlank()) {
            baseName = "resume";
        }

        String versionCode = safe(version.getVersionCode());

        return baseName + "-" + versionCode + "." + extension;
    }

    /**
     * Build a timestamp-based temporary file name.
     */
    public static String buildTempFileName(String prefix, String extension) {
        return safe(prefix)
                + "-"
                + LocalDate.now()
                + "-"
                + System.currentTimeMillis()
                + "."
                + extension;
    }

    /**
     * Sanitize a file name.
     */
    public static String sanitizeFileName(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            return "file";
        }

        return fileName
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_+", "_");
    }

    /**
     * Ensure path-safe string.
     */
    private static String safe(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }

}