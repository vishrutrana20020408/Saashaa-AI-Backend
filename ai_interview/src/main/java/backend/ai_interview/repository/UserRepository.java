package backend.ai_interview.repository;

import backend.ai_interview.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 *
 * Primary key:
 * - sNo (Long)
 *
 * Business identity:
 * - userId (String)
 * - emailAddress (String)
 *
 * Used for:
 * - authentication
 * - onboarding checks
 * - resume/profile lookup
 * - navbar/profile resolution
 */
@Repository
@SuppressWarnings("all")
public interface UserRepository extends JpaRepository<AppUser, Long> {

    /* =========================================================
       Auth / Identity
    ========================================================= */
    Optional<AppUser> findByUserId(String userId);

    Optional<AppUser> findByEmailAddress(String emailAddress);

    Optional<AppUser> findByEmailAddressIgnoreCase(String emailAddress);

    Optional<AppUser> findByShareId(String shareId);

    boolean existsByUserId(String userId);

    boolean existsByEmailAddress(String emailAddress);

    boolean existsByShareId(String shareId);

    /* =========================================================
       Onboarding / Resume flags
    ========================================================= */
    boolean existsByUserIdAndResumeScannedTrue(String userId);

    boolean existsByUserIdAndOnboardingDoneTrue(String userId);

    /* =========================================================
       Profile helpers
    ========================================================= */
    boolean existsByUserIdAndProfileCreatedTrue(String userId);

    boolean existsByUserIdAndSourceResumeVersionIdIsNotNull(String userId);
}