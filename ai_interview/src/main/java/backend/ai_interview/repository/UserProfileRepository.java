package backend.ai_interview.repository;

import backend.ai_interview.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserProfile Repository
 *
 * Handles database access for official user profiles used by:
 * - navbar profile summary
 * - user profile page
 * - resume-to-profile sync flow
 * - manual profile editing
 */
@Repository
@SuppressWarnings("all")
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Find profile by linked AppUser userId
     */
    Optional<UserProfile> findByUser_UserId(String userId);

    /**
     * Check whether a profile exists for the linked AppUser userId
     */
    boolean existsByUser_UserId(String userId);

    /**
     * Find profile by linked AppUser email
     */
    Optional<UserProfile> findByUser_EmailAddress(String emailAddress);

    /**
     * Find profile by linked AppUser shareId
     */
    Optional<UserProfile> findByUser_ShareId(String shareId);

    /**
     * Check whether profile has been synced from a resume version
     */
    boolean existsByUser_UserIdAndSourceResumeVersionIdIsNotNull(String userId);

    /**
     * Find profile by source resume version id
     */
    Optional<UserProfile> findBySourceResumeVersionId(Long sourceResumeVersionId);
}