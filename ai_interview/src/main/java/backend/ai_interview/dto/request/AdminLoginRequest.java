package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin Login Request DTO
 */
@Getter
@SuppressWarnings("all")
@Setter
public class AdminLoginRequest {

    @NotBlank(message = "Email Address is required")
    private String emailAddress;

    @NotBlank(message = "Password is required")
    private String password;
}