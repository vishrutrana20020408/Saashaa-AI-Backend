package backend.ai_interview.service.auth;

import org.springframework.stereotype.Service;

import backend.ai_interview.dto.request.OwnerLoginRequest;
import backend.ai_interview.dto.request.OwnerRegisterRequest;
import backend.ai_interview.dto.response.AuthResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Owner;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.OwnerRepository;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.security.JwtService;
import backend.ai_interview.security.Roles;

@Service
@SuppressWarnings("all")
public class OwnerAuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public OwnerAuthService(UserRepository userRepository,
                            AdminRepository adminRepository,
                            OwnerRepository ownerRepository,
                            PasswordService passwordService,
                            JwtService jwtService) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.ownerRepository = ownerRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(OwnerRegisterRequest request) {
        // Validate email is provided
        if (request.getEmailAddress() == null || request.getEmailAddress().isBlank()) {
            throw new ApiException("Email address is required");
        }

        // Check if email is already registered in owner database for owner role
        // Per requirement: if email is not registered in owner database, allow registration
        // if email is already registered in owner database, reject registration
        if (ownerRepository.findByEmailAddressIgnoreCase(request.getEmailAddress()).isPresent()) {
            throw new ApiException("Email is already registered as an owner. Please use a different email address or login to your existing account.");
        }

        // Create new owner with OWNER role
        Owner owner = Owner.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .emailAddress(request.getEmailAddress())
                .mobileNumber(request.getMobileNumber())
                .password(passwordService.hash(request.getPassword()))
                .role(Roles.OWNER)
                .build();

        owner = ownerRepository.save(owner);
        String token = jwtService.generateToken(owner.getOwnerId(), Roles.OWNER);
        return new AuthResponse(token, Roles.OWNER, owner.getOwnerId());
    }

    public AuthResponse login(OwnerLoginRequest request) {
        Owner owner = ownerRepository.findByEmailAddressIgnoreCase(request.getEmailAddress())
                .filter(existing -> Roles.OWNER.equals(existing.getRole()))
                .orElse(null);

        if (owner != null) {
            if (!passwordService.matches(request.getPassword(), owner.getPassword())) {
                throw new ApiException("Invalid owner credentials");
            }
            String token = jwtService.generateToken(owner.getOwnerId(), Roles.OWNER);
            return new AuthResponse(token, Roles.OWNER, owner.getOwnerId());
        }

        AppUser userOwner = userRepository.findByEmailAddressIgnoreCase(request.getEmailAddress())
                .filter(existing -> Roles.OWNER.equals(existing.getRole()))
                .orElse(null);

        if (userOwner != null) {
            if (!passwordService.matches(request.getPassword(), userOwner.getPassword())) {
                throw new ApiException("Invalid owner credentials");
            }
            String token = jwtService.generateToken(userOwner.getUserId(), Roles.OWNER);
            return new AuthResponse(token, Roles.OWNER, userOwner.getUserId());
        }

        Admin adminOwner = adminRepository.findByEmailAddressIgnoreCase(request.getEmailAddress())
                .filter(existing -> Roles.OWNER.equals(existing.getRole()))
                .orElse(null);

        if (adminOwner != null) {
            if (!passwordService.matches(request.getPassword(), adminOwner.getPassword())) {
                throw new ApiException("Invalid owner credentials");
            }
            String token = jwtService.generateToken(adminOwner.getAdminId(), Roles.OWNER);
            return new AuthResponse(token, Roles.OWNER, adminOwner.getAdminId());
        }

        throw new ApiException("Invalid owner credentials");
    }
}
