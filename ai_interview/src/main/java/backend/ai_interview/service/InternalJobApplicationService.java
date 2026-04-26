package backend.ai_interview.service;

import backend.ai_interview.dto.request.AdminJobApplyRequest;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.InternalJobApplication;
import backend.ai_interview.entity.Job;
import backend.ai_interview.entity.ResumeFileAsset;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.InternalJobApplicationRepository;
import backend.ai_interview.repository.ResumeFileAssetRepository;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("all")
public class InternalJobApplicationService {

    private final InternalJobApplicationRepository repository;
    private final JobService jobService;
    private final ResumeFileAssetRepository resumeFileAssetRepository;

    public InternalJobApplicationService(InternalJobApplicationRepository repository,
                                         JobService jobService,
                                         ResumeFileAssetRepository resumeFileAssetRepository) {
        this.repository = repository;
        this.jobService = jobService;
        this.resumeFileAssetRepository = resumeFileAssetRepository;
    }

    public InternalJobApplication apply(AdminJobApplyRequest request, Admin admin) {
        Job job = jobService.getJobById(request.getJobId());

        if (repository.findByAdminAndJob(admin, job).isPresent()) {
            throw new ApiException("You have already applied for this job.");
        }

        ResumeFileAsset resumeFileAsset = null;
        if ("UPLOADED".equalsIgnoreCase(request.getResumeType())) {
            if (request.getResumeFileId() == null || request.getResumeFileId().isBlank()) {
                throw new ApiException("Resume file is required when resume type is UPLOADED.");
            }
            resumeFileAsset = resumeFileAssetRepository.findByAssetCode(request.getResumeFileId())
                    .orElseThrow(() -> new ApiException("Resume file not found"));
        } else if (!"WEBSITE".equalsIgnoreCase(request.getResumeType())) {
            throw new ApiException("Invalid resume type. Must be WEBSITE or UPLOADED.");
        }

        InternalJobApplication application = InternalJobApplication.builder()
                .job(job)
                .admin(admin)
                .resumeType(request.getResumeType().toUpperCase())
                .resumeFileAsset(resumeFileAsset)
                .status("PENDING")
                .build();

        return repository.save(application);
    }
}
