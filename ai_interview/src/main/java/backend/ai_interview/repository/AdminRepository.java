package backend.ai_interview.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import backend.ai_interview.entity.Admin;

/**
 * Admin Repository
 *
 * Uses sNo (Long) as primary key, and adminId (UUID String) as unique business ID.
 */
@Repository
@SuppressWarnings("all")
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByEmailAddress(String emailAddress);

    Optional<Admin> findByEmailAddressIgnoreCase(String emailAddress);

    boolean existsByEmailAddress(String emailAddress);

    boolean existsByShareId(String shareId);

    Optional<Admin> findByAdminId(String adminId);

    boolean existsByAdminId(String adminId);
}