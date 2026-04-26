package backend.ai_interview.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@SuppressWarnings("all")
public class MockJobStore {

    private final List<Map<String, Object>> jobs = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public MockJobStore() {
        jobs.add(createJob("Frontend Engineer", "Build responsive user interfaces", true, "TECH"));
        jobs.add(createJob("Backend Engineer", "Develop REST APIs and services", false, "TECH"));
        jobs.add(createJob("Data Scientist", "Analyze hiring data and build models", false, "TECH"));
        jobs.add(createJob("HR Manager", "Manage recruitment and employee relations", false, "NON_TECH"));
        jobs.add(createJob("Content Writer", "Create engaging content for marketing", false, "NON_TECH"));
    }

    public synchronized List<Map<String, Object>> getAllJobs() {
        return new ArrayList<>(jobs);
    }

    public synchronized Map<String, Object> addJob(Map<String, Object> jobData) {
        Map<String, Object> job = new HashMap<>();
        job.put("id", idGenerator.getAndIncrement());
        job.put("title", jobData.getOrDefault("title", jobData.getOrDefault("jobTitle", "Untitled Job")));
        job.put("description", jobData.getOrDefault("description", "No description provided."));
        job.put("isRecommended", jobData.getOrDefault("isRecommended", false));
        job.put("status", jobData.getOrDefault("status", "OPEN"));
        job.put("domain", jobData.getOrDefault("domain", "TECH"));
        job.put("postedAt", jobData.getOrDefault("postedAt", java.time.LocalDate.now().toString()));
        job.put("jobRole", jobData.getOrDefault("jobRole", "HR"));
        job.put("company", jobData.getOrDefault("companyName", jobData.getOrDefault("company", "")));
        job.put("companyName", jobData.getOrDefault("companyName", jobData.getOrDefault("company", "")));
        job.put("companyType", jobData.getOrDefault("companyType", "Startup"));
        job.put("location", jobData.getOrDefault("location", ""));
        job.put("type", jobData.getOrDefault("jobType", "Work from home"));
        job.put("officeLocation", jobData.getOrDefault("officeLocation", ""));
        job.put("salary", jobData.getOrDefault("salary", ""));
        job.put("salaryRangeMin", jobData.getOrDefault("salaryRangeMin", ""));
        job.put("salaryRangeMax", jobData.getOrDefault("salaryRangeMax", ""));
        job.put("startDateType", jobData.getOrDefault("startDateType", "IMMEDIATE"));
        job.put("joinByDate", jobData.getOrDefault("joinByDate", ""));
        job.put("lastDateToApply", jobData.getOrDefault("lastDateToApply", ""));
        job.put("educationQualification", jobData.getOrDefault("educationQualification", ""));
        job.put("skillsRequired", jobData.getOrDefault("skillsRequired", ""));
        job.put("whoCanApply", jobData.getOrDefault("whoCanApply", ""));
        job.put("payload", jobData);

        jobs.add(job);
        return job;
    }

    public synchronized Map<String, Object> updateJob(Long jobId, Map<String, Object> jobData) {
        Map<String, Object> existingJob = jobs.stream()
                .filter(job -> jobId.equals(job.get("id")))
                .findFirst()
                .orElse(null);

        if (existingJob == null) {
            return null;
        }

        existingJob.put("title", jobData.getOrDefault("title", existingJob.get("title")));
        existingJob.put("description", jobData.getOrDefault("description", existingJob.get("description")));
        existingJob.put("isRecommended", jobData.getOrDefault("isRecommended", existingJob.get("isRecommended")));
        existingJob.put("status", jobData.getOrDefault("status", existingJob.get("status")));
        existingJob.put("domain", jobData.getOrDefault("domain", existingJob.get("domain")));
        existingJob.put("postedAt", jobData.getOrDefault("postedAt", existingJob.get("postedAt")));
        existingJob.put("jobRole", jobData.getOrDefault("jobRole", existingJob.get("jobRole")));
        existingJob.put("company", jobData.getOrDefault("companyName", jobData.getOrDefault("company", existingJob.get("company"))));
        existingJob.put("companyName", jobData.getOrDefault("companyName", jobData.getOrDefault("company", existingJob.get("companyName"))));
        existingJob.put("companyType", jobData.getOrDefault("companyType", existingJob.get("companyType")));
        existingJob.put("location", jobData.getOrDefault("location", existingJob.get("location")));
        existingJob.put("type", jobData.getOrDefault("jobType", existingJob.get("type")));
        existingJob.put("officeLocation", jobData.getOrDefault("officeLocation", existingJob.get("officeLocation")));
        existingJob.put("salary", jobData.getOrDefault("salary", existingJob.get("salary")));
        existingJob.put("salaryRangeMin", jobData.getOrDefault("salaryRangeMin", existingJob.get("salaryRangeMin")));
        existingJob.put("salaryRangeMax", jobData.getOrDefault("salaryRangeMax", existingJob.get("salaryRangeMax")));
        existingJob.put("startDateType", jobData.getOrDefault("startDateType", existingJob.get("startDateType")));
        existingJob.put("joinByDate", jobData.getOrDefault("joinByDate", existingJob.get("joinByDate")));
        existingJob.put("lastDateToApply", jobData.getOrDefault("lastDateToApply", existingJob.get("lastDateToApply")));
        existingJob.put("educationQualification", jobData.getOrDefault("educationQualification", existingJob.get("educationQualification")));
        existingJob.put("skillsRequired", jobData.getOrDefault("skillsRequired", existingJob.get("skillsRequired")));
        existingJob.put("whoCanApply", jobData.getOrDefault("whoCanApply", existingJob.get("whoCanApply")));
        existingJob.put("payload", jobData);

        return existingJob;
    }

    public synchronized boolean deleteJob(Long jobId) {
        return jobs.removeIf(job -> jobId.equals(job.get("id")));
    }

    private Map<String, Object> createJob(String title, String description, boolean recommended, String domain) {
        Map<String, Object> job = new HashMap<>();
        job.put("id", idGenerator.getAndIncrement());
        job.put("title", title);
        job.put("description", description);
        job.put("isRecommended", recommended);
        job.put("status", "OPEN");
        job.put("domain", domain);
        job.put("postedAt", java.time.LocalDate.now().toString());
        return job;
    }
}
