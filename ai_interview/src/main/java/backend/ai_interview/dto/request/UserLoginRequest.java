package backend.ai_interview.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * User Login Request DTO
 */
@SuppressWarnings("all")
public class UserLoginRequest {

    @NotBlank(message = "Email Address is required")
    @Pattern(
            regexp = "^[^\\s@]+@[^\\s@]+\\.com$",
            message = "Email must contain @, end with .com, and must not contain spaces"
    )
    private String emailAddress;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "CAPTCHA token is required")
    private String captchaToken;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }
}