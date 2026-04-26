package backend.ai_interview.util;

import java.util.Locale;

/**
 * ProfileFieldNormalizer
 *
 * Utility class for normalizing profile-related fields before
 * storing them in the database or returning them in responses.
 *
 * Used for:
 * - trimming and cleaning user/admin profile values
 * - standardizing email, phone, URLs, names, headlines, locations
 * - safely handling null/blank values
 */
public final class ProfileFieldNormalizer {

    private ProfileFieldNormalizer() {
        // utility class
    }

    /**
     * Normalize full name:
     * - trim
     * - collapse multiple spaces
     * - convert blank to null
     */
    public static String normalizeFullName(String value) {
        return normalizeText(value);
    }

    /**
     * Normalize headline:
     * - trim
     * - collapse multiple spaces
     * - convert blank to null
     */
    public static String normalizeHeadline(String value) {
        return normalizeText(value);
    }

    /**
     * Normalize location:
     * - trim
     * - collapse multiple spaces
     * - convert blank to null
     */
    public static String normalizeLocation(String value) {
        return normalizeText(value);
    }

    /**
     * Normalize summary/about text:
     * - trim
     * - normalize line endings
     * - collapse repeated blank lines
     * - convert blank to null
     */
    public static String normalizeSummary(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace('\u00A0', ' ')
                .replaceAll("[ \t]{2,}", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();

        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Normalize email:
     * - trim
     * - lowercase
     * - blank -> null
     */
    public static String normalizeEmail(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Normalize phone:
     * - trim
     * - keep digits, +, -, spaces, parentheses
     * - collapse multiple spaces
     * - blank -> null
     */
    public static String normalizePhone(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value
                .trim()
                .replaceAll("[^\\d+\\-() ]", "")
                .replaceAll("\\s{2,}", " ")
                .trim();

        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Normalize generic URL:
     * - trim
     * - add https:// if missing and value looks like a domain/link
     * - blank -> null
     */
    public static String normalizeUrl(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            if (normalized.startsWith("www.")
                    || normalized.contains(".com")
                    || normalized.contains(".in")
                    || normalized.contains(".org")
                    || normalized.contains(".net")
                    || normalized.contains("linkedin.")
                    || normalized.contains("github.")) {
                normalized = "https://" + normalized;
            }
        }

        return normalized;
    }

    public static String normalizeLinkedinUrl(String value) {
        return normalizeUrl(value);
    }

    public static String normalizeGithubUrl(String value) {
        return normalizeUrl(value);
    }

    public static String normalizePortfolioUrl(String value) {
        return normalizeUrl(value);
    }

    /**
     * Normalize company / role / education strings.
     */
    public static String normalizeSimpleField(String value) {
        return normalizeText(value);
    }

    /**
     * Normalize JSON text fields stored as raw strings.
     * Does not validate JSON structure, only trims safely.
     */
    public static String normalizeJsonText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Normalize any text field:
     * - trim
     * - replace NBSP
     * - collapse multiple spaces
     * - blank -> null
     */
    public static String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value
                .replace('\u00A0', ' ')
                .trim()
                .replaceAll("\\s{2,}", " ");

        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Safely choose first non-blank normalized value.
     */
    public static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            String normalized = normalizeText(value);
            if (normalized != null) {
                return normalized;
            }
        }

        return null;
    }

    /**
     * Build initials from a full name.
     * Example:
     * "Vishrut Rana" -> "VR"
     */
    public static String buildInitials(String fullName) {
        String normalized = normalizeFullName(fullName);
        if (normalized == null) {
            return null;
        }

        String[] parts = normalized.split("\\s+");
        if (parts.length == 0) {
            return null;
        }

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase(Locale.ROOT);
    }
}