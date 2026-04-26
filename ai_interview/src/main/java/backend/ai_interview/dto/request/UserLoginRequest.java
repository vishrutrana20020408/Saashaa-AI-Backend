package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * User Login Request DTO
 */
@Getter
@SuppressWarnings("all")
@Setter
public class UserLoginRequest {

    @NotBlank(message = "Email Address is required")
    @Pattern(
            regexp = "^[^\\s@]+@[^\\s@]+\\.com$",
            message = "Email must contain @, end with .com, and must not contain spaces"
    )
    private String emailAddress;

    @NotBlank(message = "Password is required")
    private String password;
}