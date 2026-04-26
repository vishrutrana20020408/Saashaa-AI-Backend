package backend.ai_interview.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import backend.ai_interview.dto.request.JobPostRequest;
import backend.ai_interview.entity.Company;
import backend.ai_interview.entity.Job;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.JobRepository;

@Service
@SuppressWarnings("all")
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public Job createJob(JobPostRequest request, Company company) {
        Job job = Job.builder()
                .title(request.getTitle() != null ? request.getTitle() : request.getPost())
                .post(request.getPost())
                .hrType(request.getHrType())
                .otherHrType(request.getOtherHrType())
                .workingType(request.getWorkingType())
                .officeLocation(request.getOfficeLocation())
                .startDateType(request.getStartDateType())
                .specificStartDate(request.getSpecificStartDate())
                .salary(request.getSalary())
                .lastDateToApply(request.getLastDateToApply())
                .description(request.getDescription())
                .skillsRequired(request.getSkillsRequired())
                .whoCanApply(request.getWhoCanApply())
                .company(company)
                .status("OPEN")
                .build();

        return jobRepository.save(job);
    }

    public Job updateJob(Long jobId, JobPostRequest request, Company company) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job not found with id: " + jobId));

        if (!job.getCompany().getCompanyId().equals(company.getCompanyId())) {
            throw new ApiException("Not authorized to update this job");
        }

        job.setTitle(request.getTitle() != null ? request.getTitle() : request.getPost());
        job.setPost(request.getPost());
        job.setHrType(request.getHrType());
        job.setOtherHrType(request.getOtherHrType());
        job.setWorkingType(request.getWorkingType());
        job.setOfficeLocation(request.getOfficeLocation());
        job.setStartDateType(request.getStartDateType());
        job.setSpecificStartDate(request.getSpecificStartDate());
        job.setSalary(request.getSalary());
        job.setLastDateToApply(request.getLastDateToApply());
        job.setDescription(request.getDescription());
        job.setSkillsRequired(request.getSkillsRequired());
        job.setWhoCanApply(request.getWhoCanApply());

        return jobRepository.save(job);
    }

    public void deleteJob(Long jobId, Company company) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ApiException("Job not found with id: " + jobId));

        if (!job.getCompany().getCompanyId().equals(company.getCompanyId())) {
            throw new ApiException("Not authorized to delete this job");
        }

        jobRepository.delete(job);
    }

    public List<Job> getJobsByCompany(Company company) {
        return jobRepository.findByCompany(company);
    }

    public List<Job> getActiveJobs() {
        return jobRepository.findActiveJobs(LocalDate.now());
    }

    public List<Job> getActiveJobsForUser(String domain) {
        if (domain == null || domain.isBlank()) {
            return getActiveJobs();
        }
        
        // Normalize domain
        String normalizedDomain = domain.toUpperCase().replace("-", "_");
        if (normalizedDomain.contains("NON_TECHNICAL") || normalizedDomain.contains("NON_TECH")) {
            normalizedDomain = "NON_TECH";
        } else if (normalizedDomain.contains("TECHNICAL") || normalizedDomain.contains("TECH")) {
            normalizedDomain = "TECH";
        }

        return jobRepository.findActiveJobsForUserDomain(LocalDate.now(), normalizedDomain);
    }

    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ApiException("Job not found with id: " + id));
    }
}
