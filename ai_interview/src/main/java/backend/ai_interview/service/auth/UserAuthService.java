package backend.ai_interview.service.auth;

import backend.ai_interview.dto.request.UserLoginRequest;
import backend.ai_interview.dto.request.UserRegisterRequest;
import backend.ai_interview.dto.response.AuthResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.security.JwtService;
import backend.ai_interview.security.Roles;

import org.springframework.stereotype.Service;

/**
 * User Authentication Service
 *
 * ✅ Users can REGISTER
 * ✅ Users can LOGIN
 * ❌ Admin registration is not allowed here
 */
@Service
@SuppressWarnings("all")
public class UserAuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public UserAuthService(UserRepository userRepository,
                           PasswordService passwordService,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    /**
     * User Registration
     *
     * - Only Users can register publicly
     * - Email must be unique
     * - Password is encrypted (BCrypt)
     * - Role set to USER
     */
    public AuthResponse register(UserRegisterRequest request) {

        if (userRepository.existsByEmailAddress(request.getEmailAddress())) {
            throw new ApiException("Email already registered");
        }

        AppUser user = AppUser.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .emailAddress(request.getEmailAddress())
                .mobileNumber(request.getMobileNumber())
                .password(passwordService.hash(request.getPassword()))
                .role(Roles.USER)
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getUserId(), Roles.USER);
        return new AuthResponse(token, Roles.USER, user.getUserId());
    }

    /**
     * User Login
     *
     * - Checks only Users table
     * - Returns JWT with role USER
     */
    public AuthResponse login(UserLoginRequest request) {

        AppUser user = userRepository.findByEmailAddress(request.getEmailAddress())
                .orElseThrow(() -> new ApiException("Invalid user credentials"));

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ApiException("User account is not correctly configured. Please contact support.");
        }

        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid user credentials");
        }

        String token = jwtService.generateToken(user.getUserId(), Roles.USER);
        return new AuthResponse(token, Roles.USER, user.getUserId());
    }
}