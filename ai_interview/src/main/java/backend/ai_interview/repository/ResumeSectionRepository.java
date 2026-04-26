package backend.ai_interview.repository;

import backend.ai_interview.entity.ResumeSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Resume Section Repository
 *
 * Handles database operations for ResumeSection entity
 * in the latest backend-integrated project structure.
 *
 * Relationships:
 * ResumeVersion (1) -> (N) ResumeSection
 */
@Repository
@SuppressWarnings("all")
public interface ResumeSectionRepository extends JpaRepository<ResumeSection, Long> {

    /**
     * Fetch all sections for a resume version.
     */
    List<ResumeSection> findByResumeVersion_ResumeVersionId(Long resumeVersionId);

    /**
     * Fetch all sections for a resume version ordered by sectionOrder.
     */
    List<ResumeSection> findByResumeVersion_ResumeVersionIdOrderBySectionOrderAsc(Long resumeVersionId);

    /**
     * Fetch a specific section by type within a resume version.
     */
    Optional<ResumeSection> findByResumeVersion_ResumeVersionIdAndSectionType(
            Long resumeVersionId,
            String sectionType
    );

    /**
     * Fetch all sections of a specific type.
     */
    List<ResumeSection> findBySectionType(String sectionType);

    /**
     * Fetch sections for a specific user.
     * Path: ResumeSection -> ResumeVersion -> Resume -> User
     */
    List<ResumeSection> findByResumeVersion_Resume_User_UserId(String userId);

    /**
     * Fetch sections for a specific user ordered by version and section order.
     */
    List<ResumeSection> findByResumeVersion_Resume_User_UserIdOrderByResumeVersion_ResumeVersionIdAscSectionOrderAsc(
            String userId
    );

    /**
     * Fetch sections for a resume owned by a user.
     */
    List<ResumeSection> findByResumeVersion_Resume_ResumeIdAndResumeVersion_Resume_User_UserId(
            Long resumeId,
            String userId
    );

    /**
     * Delete all sections belonging to a resume version.
     */
    void deleteByResumeVersion_ResumeVersionId(Long resumeVersionId);

    /**
     * Count sections in a resume version.
     */
    long countByResumeVersion_ResumeVersionId(Long resumeVersionId);
}