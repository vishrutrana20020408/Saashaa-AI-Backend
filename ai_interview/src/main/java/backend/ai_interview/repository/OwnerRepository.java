package backend.ai_interview.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.ai_interview.entity.Owner;

@Repository
@SuppressWarnings("all")
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<Owner> findByOwnerId(String ownerId);

    Optional<Owner> findByEmailAddress(String emailAddress);

    Optional<Owner> findByEmailAddressIgnoreCase(String emailAddress);

    boolean existsByEmailAddress(String emailAddress);

    boolean existsByShareId(String shareId);
}
