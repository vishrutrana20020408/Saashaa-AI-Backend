package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Company Registration Request DTO
 */
@Getter
@SuppressWarnings("all")
@Setter
public class CompanyRegisterRequest {

    @NotBlank(message = "Company Name is required")
    private String companyName;

    @NotBlank(message = "Company Type is required")
    private String companyType;

    @NotBlank(message = "Contact Person Name is required")
    private String contactPersonName;

    @NotBlank(message = "Email Address is required")
    @Pattern(
            regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$",
            message = "Email must contain @, a valid domain, and must not contain spaces"
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
