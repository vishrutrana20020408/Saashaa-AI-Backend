package backend.ai_interview.service.resume;

import backend.ai_interview.dto.response.ResumeResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.Resume;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.exception.ResumeNotFoundException;
import backend.ai_interview.repository.ResumeRepository;
import backend.ai_interview.repository.UserRepository;
import backend.ai_interview.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resume Service
 *
 * Handles resume read operations for:
 * - user-side resume listing
 * - active resume lookup
 * - latest/current resume lookup
 * - single resume fetch by id/code
 * - lightweight resume counting
 *
 * Latest project alignment:
 * - resolves authenticated principal as either userId or email
 * - stays compatible with frontend current-resume endpoints
 * - aligns with broader resume/version/editor/tailoring architecture
 */
@Service
@SuppressWarnings("all")
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public ResumeService(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            AdminRepository adminRepository
    ) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    /**
     * Fetch all resumes belonging to the authenticated user.
     * The input may be userId or email depending on the security principal.
     */
    @Transactional(readOnly = true)
    public List<ResumeResponse> getResumesByUser(String principal) {
        AppUser user = resolveUser(principal);

        return resumeRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetch all active resumes belonging to the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<ResumeResponse> getActiveResumesByUser(String principal) {
        AppUser user = resolveUser(principal);

        return resumeRepository.findByUser_UserIdAndStatusOrderByCreatedAtDesc(user.getUserId(), "ACTIVE")
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Fetch the latest/current resume of the authenticated user.
     */
    @Transactional(readOnly = true)
    public ResumeResponse getLatestResumeForUser(String principal) {
        AppUser user = resolveUser(principal);

        Resume latest = resumeRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResumeNotFoundException("No resume found for this user"));

        return toResponse(latest);
    }

    /**
     * Fetch a single resume by database id.
     * Primarily used by admin inspection flows.
     */
    @Transactional(readOnly = true)
    public ResumeResponse getById(Long resumeId) {
        if (resumeId == null) {
            throw new ApiException("Resume id is required");
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found"));

        return toResponse(resume);
    }

    /**
     * Fetch a single resume by database id only if it belongs to the given user.
     * The user identifier may be principal email or userId.
     */
    @Transactional(readOnly = true)
    public ResumeResponse getById(String principal, Long resumeId) {
        AppUser user = resolveUser(principal);

        if (resumeId == null) {
            throw new ApiException("Resume id is required");
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found"));

        if (resume.getUser() == null || !user.getUserId().equals(resume.getUser().getUserId())) {
            throw new ResumeNotFoundException("Resume not found for this user");
        }

        return toResponse(resume);
    }

    /**
     * Fetch a resume by public resume code.
     */
    @Transactional(readOnly = true)
    public ResumeResponse getByResumeCode(String resumeCode) {
        if (resumeCode == null || resumeCode.trim().isEmpty()) {
            throw new ApiException("Resume code is required");
        }

        Resume resume = resumeRepository.findByResumeCode(resumeCode.trim())
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found"));

        return toResponse(resume);
    }

    /**
     * Count total resumes of the authenticated user.
     */
    @Transactional(readOnly = true)
    public long countUserResumes(String principal) {
        AppUser user = resolveUser(principal);
        return resumeRepository.countByUser_UserId(user.getUserId());
    }

    private AppUser resolveUser(String principal) {
        validatePrincipal(principal);

        String normalized = principal.trim();

        // Try resolving as AppUser by userId first
        Optional<AppUser> userOptional = userRepository.findByUserId(normalized);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }

        // If not found by userId, try resolving by email address
        userOptional = userRepository.findByEmailAddress(normalized);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }

        // If still not found as AppUser, try resolving as Admin
        Optional<Admin> adminOptional = adminRepository.findByAdminId(normalized);
        if (adminOptional.isEmpty()) {
            adminOptional = adminRepository.findByEmailAddress(normalized);
        }

        if (adminOptional.isPresent()) {
            // If Admin found, try to find the corresponding AppUser by email
            userOptional = userRepository.findByEmailAddress(adminOptional.get().getEmailAddress());
            if (userOptional.isPresent()) {
                return userOptional.get();
            }
        }

        // If still not found, throw an exception.
        throw new ApiException("User not found for principal: " + principal);
    }

    private ResumeResponse toResponse(Resume resume) {
        if (resume == null) {
            return ResumeResponse.fail("Resume not found");
        }

        AppUser user = resume.getUser();

        String userId = null;
        String userEmail = null;
        String userName = null;

        if (user != null) {
            userId = user.getUserId();
            userEmail = user.getEmailAddress();
            userName = buildFullName(user);
        }

        ResumeResponse response = ResumeResponse.of(
                resume.getResumeId(),
                resume.getResumeCode(),
                resume.getTitle(),
                resume.getDescription(),
                resume.getStatus(),
                resume.getOriginalFileName(),
                resume.getOriginalFileUrl(),
                resume.getCurrentBaseVersionCode(),
                userId,
                userEmail,
                userName,
                resume.getTotalVersions(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );

        return response;
    }

    private String buildFullName(AppUser user) {
        String first = user.getName() == null ? "" : user.getName().trim();
        String last = user.getSurname() == null ? "" : user.getSurname().trim();
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }

    private void validatePrincipal(String principal) {
        if (principal == null || principal.trim().isEmpty()) {
            throw new ApiException("Invalid user session. Please login again.");
        }
    }
}