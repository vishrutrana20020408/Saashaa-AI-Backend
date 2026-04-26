package backend.ai_interview.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic API Response DTO
 *
 * ✅ Standard structure:
 * {
 *   success: boolean,
 *   message: string,
 *   data?: T
 * }
 *
 * ✅ Works with:
 * - String
 * - DTOs
 * - Lists
 * - Maps
 */
@Getter
@SuppressWarnings("all")
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates success or failure
     */
    private boolean success;

    /**
     * Message describing the result
     */
    private String message;

    /**
     * Generic response payload
     */
    private T data;

    // ✅ Constructor without data
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    // =========================
    // ✅ SUCCESS FACTORIES
    // =========================

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // =========================
    // ❌ FAILURE FACTORIES
    // =========================

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> ApiResponse<T> fail(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}