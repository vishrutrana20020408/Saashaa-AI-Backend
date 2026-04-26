package backend.ai_interview.repository;

import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AdminProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AdminProfile Repository
 *
 * Handles database access for official admin profiles used by:
 * - navbar profile summary
 * - admin profile page
 * - resume-to-profile sync flow
 * - manual profile editing
 */
@Repository
@SuppressWarnings("all")
public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {

    /**
     * Find profile by linked Admin entity
     */
    Optional<AdminProfile> findByAdmin(Admin admin);

    /**
     * Check whether profile exists for admin entity
     */
    boolean existsByAdmin(Admin admin);

    /**
     * Find profile by linked Admin adminId
     */
    Optional<AdminProfile> findByAdmin_AdminId(String adminId);

    /**
     * Check whether profile exists for admin
     */
    boolean existsByAdmin_AdminId(String adminId);

    /**
     * Find profile by admin email
     */
    Optional<AdminProfile> findByAdmin_EmailAddress(String emailAddress);

    /**
     * Check if profile was synced from a resume version
     */
    boolean existsByAdmin_AdminIdAndSourceResumeVersionIdIsNotNull(String adminId);

    /**
     * Find profile by resume version that synced it
     */
    Optional<AdminProfile> findBySourceResumeVersionId(Long sourceResumeVersionId);
}