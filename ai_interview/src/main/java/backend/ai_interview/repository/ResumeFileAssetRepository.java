package backend.ai_interview.repository;

import backend.ai_interview.entity.ResumeFileAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Resume File Asset Repository
 *
 * Handles database operations for ResumeFileAsset entity
 * in the latest backend-integrated project structure.
 */
@Repository
@SuppressWarnings("all")
public interface ResumeFileAssetRepository extends JpaRepository<ResumeFileAsset, Long> {

    /**
     * Find file asset by public asset code.
     */
    Optional<ResumeFileAsset> findByAssetCode(String assetCode);

    /**
     * Check if file asset exists by asset code.
     */
    boolean existsByAssetCode(String assetCode);

    /**
     * Fetch all file assets by asset type.
     */
    List<ResumeFileAsset> findByAssetType(String assetType);

    /**
     * Fetch all file assets ordered by newest first.
     */
    List<ResumeFileAsset> findAllByOrderByCreatedAtDesc();

    /**
     * Fetch all file assets by content type.
     */
    List<ResumeFileAsset> findByContentType(String contentType);

    /**
     * Fetch file asset by stored file name.
     */
    Optional<ResumeFileAsset> findByStoredFileName(String storedFileName);

    /**
     * Fetch file assets by original file name.
     */
    List<ResumeFileAsset> findByFileName(String fileName);

    /**
     * Fetch file asset by checksum.
     */
    Optional<ResumeFileAsset> findByChecksum(String checksum);

    /**
     * Count file assets by asset type.
     */
    long countByAssetType(String assetType);
}