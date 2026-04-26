package backend.ai_interview.repository;

import backend.ai_interview.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Company Repository
 *
 * Uses sNo (Long) as primary key, and companyId (UUID String) as unique business ID.
 */
@Repository
@SuppressWarnings("all")
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByEmailAddress(String emailAddress);

    boolean existsByEmailAddress(String emailAddress);

    boolean existsByShareId(String shareId);

    Optional<Company> findByCompanyId(String companyId);

    boolean existsByCompanyId(String companyId);
}
