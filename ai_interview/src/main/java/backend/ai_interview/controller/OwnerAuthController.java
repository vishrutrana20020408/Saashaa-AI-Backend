package backend.ai_interview.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.ai_interview.dto.request.OwnerLoginRequest;
import backend.ai_interview.dto.request.OwnerRegisterRequest;
import backend.ai_interview.dto.response.AuthResponse;
import backend.ai_interview.service.auth.OwnerAuthService;
import jakarta.validation.Valid;

@RestController
@SuppressWarnings("all")
@RequestMapping({"/api/owner/auth", "/api/auth/owner"})
public class OwnerAuthController {

    private final OwnerAuthService ownerAuthService;

    public OwnerAuthController(OwnerAuthService ownerAuthService) {
        this.ownerAuthService = ownerAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody OwnerLoginRequest request) {
        AuthResponse response = ownerAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody OwnerRegisterRequest request) {
        AuthResponse response = ownerAuthService.register(request);
        return ResponseEntity.ok(response);
    }
}
