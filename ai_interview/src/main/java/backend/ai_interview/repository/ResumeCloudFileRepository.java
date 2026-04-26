package backend.ai_interview.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.ai_interview.entity.ResumeCloudFile;

/**
 * ResumeCloudFile Repository
 *
 * Provides database access for cloud-stored resume files
 */
@Repository
public interface ResumeCloudFileRepository extends JpaRepository<ResumeCloudFile, Long> {

    /**
     * Find cloud file by file ID
     */
    Optional<ResumeCloudFile> findByFileId(String fileId);

    /**
     * Find cloud files by owner user ID
     */
    List<ResumeCloudFile> findByOwnerUserId(String userId);

    /**
     * Find active cloud files by owner user ID
     */
    List<ResumeCloudFile> findByOwnerUserIdAndIsActiveTrue(String userId);

    /**
     * Find cloud file by file ID and owner user ID
     */
    Optional<ResumeCloudFile> findByFileIdAndOwnerUserId(String fileId, String userId);

    /**
     * Find cloud file by file name and owner user ID
     */
    Optional<ResumeCloudFile> findByFileNameAndOwnerUserId(String fileName, String userId);

    /**
     * Count files by owner user ID
     */
    long countByOwnerUserId(String userId);
}
