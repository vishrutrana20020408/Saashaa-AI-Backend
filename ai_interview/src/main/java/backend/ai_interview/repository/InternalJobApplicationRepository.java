package backend.ai_interview.repository;

import backend.ai_interview.entity.InternalJobApplication;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@SuppressWarnings("all")
public interface InternalJobApplicationRepository extends JpaRepository<InternalJobApplication, Long> {
    Optional<InternalJobApplication> findByAdminAndJob(Admin admin, Job job);

    boolean existsByJob_IdAndAdmin_AdminId(Long jobId, String adminId);

    java.util.List<InternalJobApplication> findByJob_Company_CompanyIdOrderByAppliedAtDesc(String companyId);
}
