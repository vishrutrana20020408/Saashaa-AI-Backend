package backend.ai_interview.service.auth;

import backend.ai_interview.dto.request.AdminLoginRequest;
import backend.ai_interview.dto.request.AdminRegisterRequest;
import backend.ai_interview.dto.response.AuthResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.security.JwtService;
import backend.ai_interview.security.Roles;
import backend.ai_interview.service.NotificationService;

import org.springframework.stereotype.Service;

/**
 * Admin Authentication Service
 *
 * ✅ Admin can LOGIN
 * ❌ Admin cannot register via public API
 *
 * Admin creation should happen via:
 * - SQL seed (recommended), OR
 * - a protected admin-only endpoint (optional)
 */
@Service
@SuppressWarnings("all")
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public AdminAuthService(AdminRepository adminRepository,
                            PasswordService passwordService,
                            JwtService jwtService,
                            NotificationService notificationService) {
        this.adminRepository = adminRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
    }

    /**
     * Admin Registration
     *
     * Allows admins to create an account from the public admin registration page.
     */
    public AuthResponse register(AdminRegisterRequest request) {
        if (adminRepository.existsByEmailAddress(request.getEmailAddress())) {
            throw new ApiException("Email already registered");
        }

        Admin admin = Admin.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .emailAddress(request.getEmailAddress())
                .mobileNumber(request.getMobileNumber())
                .password(passwordService.hash(request.getPassword()))
                .role(Roles.ADMIN)
                .build();

        admin = adminRepository.save(admin);
        String token = jwtService.generateToken(admin.getAdminId(), Roles.ADMIN);

        // Create notification for owner
        notificationService.createNotification(
            "Admin Registration",
            "A new admin '" + admin.getName() + " " + admin.getSurname() + "' has registered in your platform.",
            "ADMIN_REGISTRATION"
        );

        return new AuthResponse(token, Roles.ADMIN, admin.getAdminId());
    }

    /**
     * Admin Login
     *
     * Checks credentials only in Admin table.
     * If valid, returns JWT token with role ADMIN.
     */
    public AuthResponse login(AdminLoginRequest request) {

        Admin admin = adminRepository.findByEmailAddress(request.getEmailAddress())
                .orElseThrow(() -> new ApiException("Invalid admin credentials"));

        if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            throw new ApiException("Admin account is not correctly configured. Please contact support.");
        }

        if (!passwordService.matches(request.getPassword(), admin.getPassword())) {
            throw new ApiException("Invalid admin credentials");
        }

        String token = jwtService.generateToken(admin.getAdminId(), Roles.ADMIN);
        return new AuthResponse(token, Roles.ADMIN, admin.getAdminId());
    }
}