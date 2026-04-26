package backend.ai_interview.util.mapper;

import backend.ai_interview.dto.response.ResumeResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Resume;
import org.springframework.stereotype.Component;

/**
 * Resume Mapper
 *
 * Maps Resume entity to ResumeResponse DTO.
 *
 * Latest project alignment:
 * - keeps resume response compatible with backend-integrated frontend
 * - preserves user identity and display metadata
 * - supports current/latest resume flow and resume-version aware UI compatibility
 */
@Component
@SuppressWarnings("all")
public class ResumeMapper {

    /**
     * Convert Resume entity to ResumeResponse DTO.
     */
    public ResumeResponse toResponse(Resume resume) {
        if (resume == null) {
            return ResumeResponse.fail("Resume not found");
        }

        AppUser user = resume.getUser();

        String userId = null;
        String userEmail = null;
        String userName = null;

        if (user != null) {
            userId = user.getUserId();
            userEmail = normalizeEmail(user.getEmailAddress());
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
        String firstName = user.getName() == null ? "" : user.getName().trim();
        String lastName = user.getSurname() == null ? "" : user.getSurname().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? null : fullName;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }
}