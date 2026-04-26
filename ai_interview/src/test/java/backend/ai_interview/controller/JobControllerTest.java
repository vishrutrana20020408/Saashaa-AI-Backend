package backend.ai_interview.controller;

import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.Job;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.CompanyRepository;
import backend.ai_interview.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class JobControllerTest {

    @Mock
    private JobService jobService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JobController jobController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetActiveJobs_AdminTech() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin-1");

        Admin admin = new Admin();
        admin.setAdminId("admin-1");
        admin.setOnboardingDomain("Technical");

        when(adminRepository.findByAdminId("admin-1")).thenReturn(Optional.of(admin));
        when(jobService.getActiveJobsForUser("TECH")).thenReturn(Collections.singletonList(new Job()));

        ResponseEntity<?> response = jobController.getActiveJobs(authentication);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testGetJobDetails() {
        Job job = new Job();
        job.setId(1L);
        when(jobService.getJobById(1L)).thenReturn(job);

        ResponseEntity<?> response = jobController.getJob(1L);

        assertEquals(200, response.getStatusCode().value());
    }
}
