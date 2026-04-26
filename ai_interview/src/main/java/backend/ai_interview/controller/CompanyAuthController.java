package backend.ai_interview.controller;

import backend.ai_interview.dto.request.CompanyLoginRequest;
import backend.ai_interview.dto.request.CompanyRegisterRequest;
import backend.ai_interview.dto.response.AuthResponse;
import backend.ai_interview.service.auth.CompanyAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Company Authentication Controller
 */
@RestController
@SuppressWarnings("all")
@RequestMapping("/api/auth/company")
public class CompanyAuthController {

    private final CompanyAuthService companyAuthService;

    public CompanyAuthController(CompanyAuthService companyAuthService) {
        this.companyAuthService = companyAuthService;
    }

    /**
     * COMPANY LOGIN
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody CompanyLoginRequest request
    ) {
        AuthResponse response = companyAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * COMPANY REGISTRATION
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody CompanyRegisterRequest request
    ) {
        AuthResponse response = companyAuthService.register(request);
        return ResponseEntity.ok(response);
    }
}
