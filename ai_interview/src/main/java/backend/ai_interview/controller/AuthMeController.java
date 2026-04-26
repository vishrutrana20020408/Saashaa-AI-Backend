package backend.ai_interview.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Company;
import backend.ai_interview.entity.Owner;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.CompanyRepository;
import backend.ai_interview.repository.OwnerRepository;
import backend.ai_interview.repository.UserRepository;

@RestController
@SuppressWarnings("all")
@RequestMapping("/api/auth")
@Transactional
public class AuthMeController {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final CompanyRepository companyRepository;
    private final OwnerRepository ownerRepository;

    public AuthMeController(UserRepository userRepository, AdminRepository adminRepository, CompanyRepository companyRepository, OwnerRepository ownerRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.companyRepository = companyRepository;
        this.ownerRepository = ownerRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("authenticated", false);
                payload.put("valid", false);
                payload.put("message", "User is not authenticated.");
                return ResponseEntity.ok(ApiResponse.success("Unauthenticated session", payload));
            }

            String principal = authentication.getName();
            String role = resolveRole(authentication.getAuthorities());
            Map<String, Object> payload = null;

            if ("OWNER".equalsIgnoreCase(role)) {
                Owner owner = ownerRepository.findByOwnerId(principal)
                        .or(() -> ownerRepository.findByEmailAddress(principal))
                        .orElse(null);
                if (owner != null) {
                    payload = buildOwnerPayload(owner);
                }
            } else if ("ADMIN".equalsIgnoreCase(role)) {
                Admin admin = adminRepository.findByAdminId(principal)
                        .or(() -> adminRepository.findByEmailAddress(principal))
                        .orElse(null);
                if (admin != null) {
                    payload = buildAdminPayload(admin);
                }
            } else if ("COMPANY".equalsIgnoreCase(role)) {
                Company company = companyRepository.findByCompanyId(principal)
                        .or(() -> companyRepository.findByEmailAddress(principal))
                        .orElse(null);
                if (company != null) {
                    payload = buildCompanyPayload(company);
                }
            } else {
                // Default to USER
                AppUser user = userRepository.findByUserId(principal)
                        .or(() -> userRepository.findByEmailAddress(principal))
                        .orElse(null);
                if (user != null) {
                    payload = buildUserPayload(user);
                }
            }

            if (payload == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail("User session not found."));
            }

            return ResponseEntity.ok(ApiResponse.success("Authenticated session is valid", payload));
        } catch (Exception ex) {
            System.err.println("Error in /api/auth/me: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Failed to verify authentication: " + ex.getMessage()));
        }
    }

    /**
     * AUTH LOGOUT
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    private String resolveRole(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return null;
        }

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role == null) {
                continue;
            }
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            if ("USER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "COMPANY".equalsIgnoreCase(role) || "OWNER".equalsIgnoreCase(role)) {
                return role.toUpperCase();
            }
        }

        return null;
    }

    private Map<String, Object> buildUserPayload(AppUser user) {
        String fullName = buildFullName(user);
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "User session is valid");
        data.put("authenticated", true);
        data.put("valid", true);
        data.put("id", user.getUserId());
        data.put("userId", user.getUserId());
        data.put("email", user.getEmailAddress());
        data.put("name", fullName);
        data.put("fullName", fullName);
        data.put("firstName", user.getName());
        data.put("lastName", user.getSurname());
        data.put("role", user.getRole());
        data.put("userRole", user.getRole());
        data.put("roles", List.of(user.getRole()));
        data.put("onboardingDone", user.isOnboardingDone());
        data.put("userOnboardingDone", user.isOnboardingDone());
        return data;
    }

    private Map<String, Object> buildOwnerPayload(Owner owner) {
        String fullName = buildFullName(owner);
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Owner session is valid");
        data.put("authenticated", true);
        data.put("valid", true);
        data.put("id", owner.getOwnerId());
        data.put("ownerId", owner.getOwnerId());
        data.put("email", owner.getEmailAddress());
        data.put("name", fullName);
        data.put("fullName", fullName);
        data.put("firstName", owner.getName());
        data.put("lastName", owner.getSurname());
        data.put("role", owner.getRole());
        data.put("userRole", owner.getRole());
        data.put("roles", List.of(owner.getRole()));
        return data;
    }

    private Map<String, Object> buildAdminPayload(Admin admin) {
        String fullName = buildFullName(admin);
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Admin session is valid");
        data.put("authenticated", true);
        data.put("valid", true);
        data.put("id", admin.getAdminId());
        data.put("adminId", admin.getAdminId());
        data.put("email", admin.getEmailAddress());
        data.put("name", fullName);
        data.put("fullName", fullName);
        data.put("firstName", admin.getName());
        data.put("lastName", admin.getSurname());
        data.put("role", admin.getRole());
        data.put("userRole", admin.getRole());
        data.put("roles", List.of(admin.getRole()));
        return data;
    }

    private Map<String, Object> buildCompanyPayload(Company company) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Company session is valid");
        data.put("authenticated", true);
        data.put("valid", true);
        data.put("id", company.getCompanyId());
        data.put("companyId", company.getCompanyId());
        data.put("email", company.getEmailAddress());
        data.put("name", company.getCompanyName());
        data.put("companyName", company.getCompanyName());
        data.put("contactPerson", company.getContactPersonName());
        data.put("companyType", company.getCompanyType());
        data.put("role", company.getRole());
        data.put("userRole", company.getRole());
        data.put("roles", List.of(company.getRole()));
        return data;
    }

    private String buildFullName(AppUser user) {
        String first = user.getName() == null ? "" : user.getName().trim();
        String last = user.getSurname() == null ? "" : user.getSurname().trim();
        String result = (first + " " + last).trim();
        return result.isBlank() ? null : result;
    }

    private String buildFullName(Admin admin) {
        String first = admin.getName() == null ? "" : admin.getName().trim();
        String last = admin.getSurname() == null ? "" : admin.getSurname().trim();
        String result = (first + " " + last).trim();
        return result.isBlank() ? null : result;
    }

    private String buildFullName(Owner owner) {
        String first = owner.getName() == null ? "" : owner.getName().trim();
        String last = owner.getSurname() == null ? "" : owner.getSurname().trim();
        String result = (first + " " + last).trim();
        return result.isBlank() ? null : result;
    }
}
