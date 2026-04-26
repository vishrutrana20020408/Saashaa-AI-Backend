package backend.ai_interview.util.mapper;

import backend.ai_interview.dto.response.ResumeVersionResponse;
import backend.ai_interview.entity.Resume;
import backend.ai_interview.entity.ResumeVersion;
import org.springframework.stereotype.Component;

/**
 * Resume Version Mapper
 *
 * Maps ResumeVersion entity to ResumeVersionResponse DTO.
 *
 * Latest project update:
 * - ResumeVersion now also supports parsed profile snapshot JSON
 * - ResumeVersion now also supports format metadata JSON
 *
 * These fields are preserved in the entity for preview/profile sync flows.
 * This mapper continues returning the existing ResumeVersionResponse DTO
 * without changing its contract.
 */
@Component
@SuppressWarnings("all")
public class ResumeVersionMapper {

    /**
     * Convert ResumeVersion entity to ResumeVersionResponse DTO.
     */
    public ResumeVersionResponse toResponse(ResumeVersion version) {
        if (version == null) {
            return ResumeVersionResponse.fail("Resume version not found");
        }

        Resume resume = version.getResume();
        ResumeVersion parentVersion = version.getParentVersion();

        ResumeVersionResponse response = ResumeVersionResponse.of(
                version.getResumeVersionId(),
                version.getVersionCode(),
                version.getVersionName(),
                version.getVersionType(),
                resume != null ? resume.getResumeId() : null,
                resume != null ? resume.getResumeCode() : null,
                Boolean.TRUE.equals(version.getBaseVersion()),
                parentVersion != null ? parentVersion.getResumeVersionId() : null,
                version.getJobApplicationCode(),
                version.getFileUrl(),
                version.getPreviewUrl(),
                version.getAtsScore(),
                version.getCreatedAt(),
                version.getUpdatedAt()
        );

        return response;
    }
}