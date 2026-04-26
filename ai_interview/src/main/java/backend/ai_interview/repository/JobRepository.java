package backend.ai_interview.repository;

import backend.ai_interview.entity.Job;
import backend.ai_interview.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@SuppressWarnings("all")
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByCompany(Company company);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND j.lastDateToApply >= :today")
    List<Job> findActiveJobs(LocalDate today);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND j.lastDateToApply >= :today AND j.domain = :domain")
    List<Job> findActiveJobsByDomain(LocalDate today, String domain);

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' AND j.lastDateToApply >= :today AND (j.domain = 'NON_TECH' OR :userDomain = 'TECH')")
    List<Job> findActiveJobsForUserDomain(LocalDate today, String userDomain);
}
