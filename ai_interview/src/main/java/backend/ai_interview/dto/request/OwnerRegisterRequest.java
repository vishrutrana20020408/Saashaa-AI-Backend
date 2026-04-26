package backend.ai_interview.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@SuppressWarnings("all")
public class OwnerRegisterRequest {

    @NotBlank(message = "Name is required")
    @JsonAlias({"firstName", "name"})
    private String name;

    @NotBlank(message = "Surname is required")
    @JsonAlias({"lastName", "surname"})
    private String surname;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @JsonAlias({"email", "emailAddress"})
    private String emailAddress;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be exactly 10 digits")
    @JsonAlias({"mobileNumber", "mobile", "phone"})
    private String mobileNumber;

    @NotBlank(message = "Password is required")
    private String password;
}
