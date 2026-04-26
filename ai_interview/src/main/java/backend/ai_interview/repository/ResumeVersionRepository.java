package backend.ai_interview.repository;

import backend.ai_interview.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Resume Version Repository
 *
 * Handles database operations for ResumeVersion entity
 * in the latest backend-integrated project structure.
 *
 * Relationships:
 * Resume (1) -> (N) ResumeVersion
 *
 * Latest project update:
 * - supports parsed profile snapshot lookup
 * - supports resume format metadata lookup
 * - supports profile sync flow from resume version
 * - supports resume editor / preview / tailoring aligned lookups
 */
@Repository
@SuppressWarnings("all")
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, Long> {

    /**
     * Find resume version by public version code.
     */
    Optional<ResumeVersion> findByVersionCode(String versionCode);

    /**
     * Check if resume version exists by version code.
     */
    boolean existsByVersionCode(String versionCode);

    /**
     * Fetch all versions of a resume by resume id.
     */
    List<ResumeVersion> findByResume_ResumeId(Long resumeId);

    /**
     * Fetch all versions of a resume by resume id ordered by newest first.
     */
    List<ResumeVersion> findByResume_ResumeIdOrderByCreatedAtDesc(Long resumeId);

    /**
     * Fetch all versions of a resume by resume code.
     */
    List<ResumeVersion> findByResume_ResumeCode(String resumeCode);

    /**
     * Fetch all versions belonging to a user's resumes.
     */
    List<ResumeVersion> findByResume_User_UserId(String userId);

    /**
     * Fetch all versions belonging to a user's resumes ordered by newest first.
     */
    List<ResumeVersion> findByResume_User_UserIdOrderByCreatedAtDesc(String userId);

    /**
     * Fetch one version by version id only if it belongs to the user.
     */
    Optional<ResumeVersion> findByResumeVersionIdAndResume_User_UserId(Long resumeVersionId, String userId);

    /**
     * Fetch all versions of a resume only if owned by the user.
     */
    List<ResumeVersion> findByResume_ResumeIdAndResume_User_UserId(Long resumeId, String userId);

    /**
     * Fetch all versions of a resume only if owned by the user ordered by newest first.
     */
    List<ResumeVersion> findByResume_ResumeIdAndResume_User_UserIdOrderByCreatedAtDesc(Long resumeId, String userId);

    /**
     * Fetch base version of a resume.
     */
    Optional<ResumeVersion> findByResume_ResumeIdAndBaseVersionTrue(Long resumeId);

    /**
     * Fetch base version of a resume by resume code.
     */
    Optional<ResumeVersion> findByResume_ResumeCodeAndBaseVersionTrue(String resumeCode);

    /**
     * Fetch all child versions derived from a parent version.
     */
    List<ResumeVersion> findByParentVersion_ResumeVersionId(Long parentVersionId);

    /**
     * Fetch all child versions derived from a parent version ordered by newest first.
     */
    List<ResumeVersion> findByParentVersion_ResumeVersionIdOrderByCreatedAtDesc(Long parentVersionId);

    /**
     * Fetch versions by version type.
     */
    List<ResumeVersion> findByVersionType(String versionType);

    /**
     * Fetch versions by version type ordered by newest first.
     */
    List<ResumeVersion> findByVersionTypeOrderByCreatedAtDesc(String versionType);

    /**
     * Count versions of a resume.
     */
    long countByResume_ResumeId(Long resumeId);

    /**
     * Fetch one version by resume + version id.
     * Useful for secure profile sync and preview flows.
     */
    Optional<ResumeVersion> findByResume_ResumeIdAndResumeVersionId(Long resumeId, Long resumeVersionId);

    /**
     * Fetch one version by resume + version id only if owned by the user.
     */
    Optional<ResumeVersion> findByResume_ResumeIdAndResumeVersionIdAndResume_User_UserId(
            Long resumeId,
            Long resumeVersionId,
            String userId
    );

    /**
     * Check whether a version contains parsed profile snapshot JSON.
     */
    boolean existsByResumeVersionIdAndProfileSnapshotJsonIsNotNull(Long resumeVersionId);

    /**
     * Check whether a version contains format metadata JSON.
     */
    boolean existsByResumeVersionIdAndFormatMetadataJsonIsNotNull(Long resumeVersionId);

    /**
     * Fetch all versions of a resume that contain parsed profile snapshot data.
     */
    List<ResumeVersion> findByResume_ResumeIdAndProfileSnapshotJsonIsNotNull(Long resumeId);

    /**
     * Fetch all versions of a user that contain parsed profile snapshot data.
     */
    List<ResumeVersion> findByResume_User_UserIdAndProfileSnapshotJsonIsNotNull(String userId);

    /**
     * Fetch all versions of a user that contain format metadata.
     */
    List<ResumeVersion> findByResume_User_UserIdAndFormatMetadataJsonIsNotNull(String userId);
}