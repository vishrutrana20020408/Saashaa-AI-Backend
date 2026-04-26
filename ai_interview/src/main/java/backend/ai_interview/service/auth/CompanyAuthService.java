package backend.ai_interview.service.auth;

import backend.ai_interview.dto.request.CompanyLoginRequest;
import backend.ai_interview.dto.request.CompanyRegisterRequest;
import backend.ai_interview.dto.response.AuthResponse;
import backend.ai_interview.entity.Company;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.CompanyRepository;
import backend.ai_interview.security.JwtService;
import backend.ai_interview.security.Roles;
import backend.ai_interview.service.NotificationService;

import org.springframework.stereotype.Service;

/**
 * Company Authentication Service
 */
@Service
@SuppressWarnings("all")
public class CompanyAuthService {

    private final CompanyRepository companyRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    public CompanyAuthService(CompanyRepository companyRepository,
                             PasswordService passwordService,
                             JwtService jwtService,
                             NotificationService notificationService) {
        this.companyRepository = companyRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.notificationService = notificationService;
    }

    /**
     * Company Registration
     */
    public AuthResponse register(CompanyRegisterRequest request) {
        if (companyRepository.existsByEmailAddress(request.getEmailAddress())) {
            throw new ApiException("Email already registered");
        }

        Company company = Company.builder()
                .companyName(request.getCompanyName())
                .companyType(request.getCompanyType())
                .contactPersonName(request.getContactPersonName())
                .emailAddress(request.getEmailAddress())
                .mobileNumber(request.getMobileNumber())
                .password(passwordService.hash(request.getPassword()))
                .role(Roles.COMPANY)
                .build();

        company = companyRepository.save(company);
        String token = jwtService.generateToken(company.getCompanyId(), Roles.COMPANY);

        // Create notification for owner
        notificationService.createNotification(
            "Company Registration",
            "A new company '" + company.getCompanyName() + "' has registered in your platform.",
            "COMPANY_REGISTRATION"
        );

        return new AuthResponse(token, Roles.COMPANY, company.getCompanyId());
    }

    /**
     * Company Login
     */
    public AuthResponse login(CompanyLoginRequest request) {

        Company company = companyRepository.findByEmailAddress(request.getEmailAddress())
                .orElseThrow(() -> new ApiException("Invalid company credentials"));

        if (company.getPassword() == null || company.getPassword().isBlank()) {
            throw new ApiException("Company account is not correctly configured. Please contact support.");
        }

        if (!passwordService.matches(request.getPassword(), company.getPassword())) {
            throw new ApiException("Invalid company credentials");
        }

        String token = jwtService.generateToken(company.getCompanyId(), Roles.COMPANY);
        return new AuthResponse(token, Roles.COMPANY, company.getCompanyId());
    }
}
