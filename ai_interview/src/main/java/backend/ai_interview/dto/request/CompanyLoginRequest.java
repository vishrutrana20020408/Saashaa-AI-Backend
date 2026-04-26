package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Company Login Request DTO
 */
@Getter
@SuppressWarnings("all")
@Setter
public class CompanyLoginRequest {

    @NotBlank(message = "Email Address is required")
    private String emailAddress;

    @NotBlank(message = "Password is required")
    private String password;
}
