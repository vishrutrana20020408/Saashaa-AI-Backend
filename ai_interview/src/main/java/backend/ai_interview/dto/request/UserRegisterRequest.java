package backend.ai_interview.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * User Registration Request DTO
 *
 * Validation Rules:
 * - Name & Surname required
 * - Email must contain @ and end with .com
 * - Email must not contain spaces
 * - Mobile number must be exactly 10 digits
 * - Password minimum 8 characters
 */
@Getter
@SuppressWarnings("all")
@Setter
public class UserRegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "Email Address is required")
    @Pattern(
            regexp = "^[^\\s@]+@[^\\s@]+\\.com$",
            message = "Email must contain @, end with .com, and must not contain spaces"
    )
    private String emailAddress;

    @NotBlank(message = "Mobile Number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Mobile number must be exactly 10 digits"
    )
    private String mobileNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}