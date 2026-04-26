package backend.ai_interview.service;

import backend.ai_interview.dto.request.AdminOnboardingRequest;
import backend.ai_interview.dto.response.AdminOnboardingResponse;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AdminProfile;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.AdminProfileRepository;
import backend.ai_interview.repository.AdminRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminOnboardingService
 *
 * Saves and loads onboarding selections for Admins.
 */
@Service
@SuppressWarnings("all")
public class AdminOnboardingService {

    private final AdminRepository adminRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final ObjectMapper objectMapper;

    public AdminOnboardingService(
            AdminRepository adminRepository,
            AdminProfileRepository adminProfileRepository,
            ObjectMapper objectMapper
    ) {
        this.adminRepository = adminRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AdminOnboardingResponse save(String adminId, AdminOnboardingRequest request) {
        validateAdminId(adminId);

        if (request == null) {
            throw new ApiException("Invalid onboarding request.");
        }

        String domain = normalizeDomain(request.getDomain());
        String mode = normalizeMode(request.getSubDomainMode());

        if (isBlank(domain)) {
            throw new ApiException("Domain is required.");
        }
        if (isBlank(mode)) {
            throw new ApiException("Sub-domain mode is required.");
        }

        String single = safe(request.getSubDomainSingle());
        List<String> multi = normalizeStringList(request.getSubDomainMulti());
        List<String> jobTitles = normalizeStringList(request.getJobTitles());

        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new ApiException("Admin not found"));

        admin.setOnboardingDomain(domain);
        admin.setOnboardingSubDomainMode(mode);
        admin.setOnboardingSubDomainSingle(mode.equals("single") ? single : "");
        admin.setOnboardingSubDomainMulti(toJson(multi));
        admin.setOnboardingJobTitles(toJson(jobTitles));
        admin.setClass10MarksheetUrl(request.getClass10MarksheetUrl());
        admin.setClass12MarksheetUrl(request.getClass12MarksheetUrl());
        admin.setGraduationMarksheetUrl(request.getGraduationMarksheetUrl());
        admin.setPostGraduationMarksheetUrl(request.getPostGraduationMarksheetUrl());
        admin.setOnboardingDone(true);

        initializeAdminProfileIfMissing(admin);

        adminRepository.save(admin);

        return AdminOnboardingResponse.builder()
                .success(true)
                .message("Onboarding saved successfully")
                .done(true)
                .domain(domain)
                .subDomainMode(mode)
                .subDomainSingle(mode.equals("single") ? single : "")
                .subDomainMulti(mode.equals("multi") ? multi : Collections.emptyList())
                .jobTitles(jobTitles)
                .class10MarksheetUrl(admin.getClass10MarksheetUrl())
                .class12MarksheetUrl(admin.getClass12MarksheetUrl())
                .graduationMarksheetUrl(admin.getGraduationMarksheetUrl())
                .postGraduationMarksheetUrl(admin.getPostGraduationMarksheetUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminOnboardingResponse get(String adminId) {
        validateAdminId(adminId);

        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new ApiException("Admin not found"));

        if (!admin.isOnboardingDone()) {
            return AdminOnboardingResponse.builder()
                    .success(true)
                    .message("Onboarding not completed")
                    .done(false)
                    .domain("")
                    .subDomainMode("")
                    .subDomainSingle("")
                    .subDomainMulti(Collections.emptyList())
                    .jobTitles(Collections.emptyList())
                    .build();
        }

        String domain = safe(admin.getOnboardingDomain());
        String mode = normalizeMode(admin.getOnboardingSubDomainMode());
        String single = safe(admin.getOnboardingSubDomainSingle());

        List<String> multi = fromJsonList(admin.getOnboardingSubDomainMulti());
        List<String> jobTitles = fromJsonList(admin.getOnboardingJobTitles());

        return AdminOnboardingResponse.builder()
                .success(true)
                .message("Onboarding loaded successfully")
                .done(true)
                .domain(domain)
                .subDomainMode(mode)
                .subDomainSingle(single)
                .subDomainMulti(multi)
                .jobTitles(jobTitles)
                .class10MarksheetUrl(admin.getClass10MarksheetUrl())
                .class12MarksheetUrl(admin.getClass12MarksheetUrl())
                .graduationMarksheetUrl(admin.getGraduationMarksheetUrl())
                .postGraduationMarksheetUrl(admin.getPostGraduationMarksheetUrl())
                .build();
    }

    @Transactional
    public void reset(String adminId) {
        validateAdminId(adminId);

        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new ApiException("Admin not found"));

        admin.setOnboardingDomain("");
        admin.setOnboardingSubDomainMode("");
        admin.setOnboardingSubDomainSingle("");
        admin.setOnboardingSubDomainMulti(toJson(Collections.emptyList()));
        admin.setOnboardingJobTitles(toJson(Collections.emptyList()));
        admin.setOnboardingDone(false);

        adminRepository.save(admin);
    }

    private void validateAdminId(String adminId) {
        if (adminId == null || adminId.trim().isEmpty()) {
            throw new ApiException("Admin ID is required.");
        }
    }

    private void initializeAdminProfileIfMissing(Admin admin) {
        if (!adminProfileRepository.existsByAdmin(admin)) {
            AdminProfile profile = AdminProfile.builder()
                    .admin(admin)
                    .fullName(admin.getName() + " " + admin.getSurname())
                    .email(admin.getEmailAddress())
                    .phone(admin.getMobileNumber())
                    .verified(false)
                    .build();
            adminProfileRepository.save(profile);
        } else {
            // Update profile marksheet URLs if they changed
            AdminProfile profile = adminProfileRepository.findByAdmin(admin)
                    .orElseThrow(() -> new ApiException("Profile not found"));
            profile.setClass10MarksheetUrl(admin.getClass10MarksheetUrl());
            profile.setClass12MarksheetUrl(admin.getClass12MarksheetUrl());
            profile.setGraduationMarksheetUrl(admin.getGraduationMarksheetUrl());
            profile.setPostGraduationMarksheetUrl(admin.getPostGraduationMarksheetUrl());
            adminProfileRepository.save(profile);
        }
    }

    private String normalizeDomain(String d) {
        if (d == null) return "";
        if (d.equalsIgnoreCase("Technical")) return "Technical";
        if (d.equalsIgnoreCase("Non-Technical")) return "Non-Technical";
        return d;
    }

    private String normalizeMode(String m) {
        if (m == null) return "";
        m = m.toLowerCase().trim();
        if (m.equals("single") || m.equals("multi") || m.equals("any")) return m;
        return "";
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private List<String> normalizeStringList(List<String> list) {
        if (list == null) return Collections.emptyList();
        return list.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list != null ? list : Collections.emptyList());
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
