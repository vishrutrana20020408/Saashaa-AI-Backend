package backend.ai_interview;

import backend.ai_interview.dto.request.JobPostRequest;
import backend.ai_interview.entity.Company;
import backend.ai_interview.entity.Job;
import backend.ai_interview.repository.CompanyRepository;
import backend.ai_interview.repository.JobRepository;
import backend.ai_interview.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProjectIntegrationTest {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    @SuppressWarnings("null")
    public void testFullJobLifecycle() {
        // 1. Create a company
        Company company = Company.builder()
                .companyName("Test Tech Inc")
                .emailAddress("test@tech.com")
                .build();
        Company savedCompany = companyRepository.save(company);
        assertNotNull(savedCompany);

        // 2. Post a job
        JobPostRequest request = new JobPostRequest();
        request.setTitle("Full Stack Developer");
        request.setPost("Developer");
        request.setHrType("TECH");
        request.setWorkingType("HYBRID");
        request.setSalary("120k");
        request.setLastDateToApply(LocalDate.now().plusMonths(1));
        request.setDescription("Expert in React and Spring Boot");

        Job job = jobService.createJob(request, savedCompany);
        assertNotNull(job.getId());
        assertEquals("Full Stack Developer", job.getTitle());

        // 3. Test filtering (Admin/User Domain logic)
        List<Job> techJobs = jobService.getActiveJobsForUser("Technical");
        assertTrue(techJobs.stream().anyMatch(j -> j.getTitle().equals("Full Stack Developer")));

        // 4. Test non-tech filtering (should still see tech if the job was marked as tech, 
        // but our service logic says non-tech users only see non-tech jobs)
        // Let's create a non-tech job to verify
        JobPostRequest nonTechRequest = new JobPostRequest();
        nonTechRequest.setTitle("HR Manager");
        nonTechRequest.setPost("HR");
        nonTechRequest.setHrType("NON_TECH");
        nonTechRequest.setWorkingType("OFFICE");
        nonTechRequest.setSalary("80k");
        nonTechRequest.setLastDateToApply(LocalDate.now().plusMonths(1));
        
        Job nonTechJob = jobService.createJob(nonTechRequest, savedCompany);
        nonTechJob.setDomain("NON_TECH"); // Manually setting for test
        jobRepository.save(nonTechJob);

        List<Job> nonTechJobs = jobService.getActiveJobsForUser("Non-Technical");
        assertTrue(nonTechJobs.stream().anyMatch(j -> j.getTitle().equals("HR Manager")));
        assertFalse(nonTechJobs.stream().anyMatch(j -> j.getTitle().equals("Full Stack Developer")));
    }
}
