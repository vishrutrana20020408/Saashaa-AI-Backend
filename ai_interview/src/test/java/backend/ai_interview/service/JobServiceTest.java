package backend.ai_interview.service;

import backend.ai_interview.dto.request.JobPostRequest;
import backend.ai_interview.entity.Company;
import backend.ai_interview.entity.Job;
import backend.ai_interview.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @SuppressWarnings("null")
    public void testCreateJob() {
        JobPostRequest request = new JobPostRequest();
        request.setTitle("Software Engineer");
        request.setPost("Engineer");
        request.setHrType("TECH");
        request.setWorkingType("REMOTE");
        request.setSalary("100k");
        request.setLastDateToApply(LocalDate.now().plusDays(10));

        Company company = new Company();
        company.setCompanyId("comp-1");

        Job job = Job.builder()
                .title("Software Engineer")
                .company(company)
                .build();

        Job savedJob = job;
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        Job createdJob = jobService.createJob(request, company);

        assertNotNull(createdJob);
        assertEquals("Software Engineer", createdJob.getTitle());
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    public void testGetActiveJobsForUser_Tech() {
        when(jobRepository.findActiveJobsForUserDomain(any(LocalDate.class), eq("TECH")))
                .thenReturn(Collections.singletonList(new Job()));

        List<Job> jobs = jobService.getActiveJobsForUser("Technical");

        assertFalse(jobs.isEmpty());
        verify(jobRepository, times(1)).findActiveJobsForUserDomain(any(LocalDate.class), eq("TECH"));
    }

    @Test
    public void testGetActiveJobsForUser_NonTech() {
        when(jobRepository.findActiveJobsForUserDomain(any(LocalDate.class), eq("NON_TECH")))
                .thenReturn(Collections.singletonList(new Job()));

        List<Job> jobs = jobService.getActiveJobsForUser("Non-Technical");

        assertFalse(jobs.isEmpty());
        verify(jobRepository, times(1)).findActiveJobsForUserDomain(any(LocalDate.class), eq("NON_TECH"));
    }

    @Test
    public void testGetJobById() {
        Job job = new Job();
        job.setId(1L);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        Job foundJob = jobService.getJobById(1L);

        assertNotNull(foundJob);
        assertEquals(1L, foundJob.getId());
    }
}
