package backend.ai_interview.service.resume;

import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Resume;
import backend.ai_interview.entity.ResumeVersion;
import backend.ai_interview.exception.ApiException;
import backend.ai_interview.repository.ResumeRepository;
import backend.ai_interview.repository.ResumeVersionRepository;
import backend.ai_interview.service.integration.ai.AtsClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Resume Ats Service
 *
 * Handles ATS scoring and automatic resume improvement.
 */
@Service
@SuppressWarnings("all")
public class ResumeAtsService {

    private final AtsClient atsClient;
    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository resumeVersionRepository;

    public ResumeAtsService(
            AtsClient atsClient,
            ResumeRepository resumeRepository,
            ResumeVersionRepository resumeVersionRepository
    ) {
        this.atsClient = atsClient;
        this.resumeRepository = resumeRepository;
        this.resumeVersionRepository = resumeVersionRepository;
    }

    /**
     * Analyze resume and improve if score < 60.
     * Also enforces score >= previous score rule.
     */
    @Transactional
    public AtsClient.AtsScoreResult processResumeAts(AppUser user, String resumeText, String jobDescription, String jobTitle) {
        // 1. Get previous score if exists
        Integer previousScore = 0;
        Optional<Resume> latestResume = resumeRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId())
                .stream().findFirst();
        
        if (latestResume.isPresent()) {
            Resume resume = latestResume.get();
            List<ResumeVersion> versions = resumeVersionRepository.findByResume_ResumeIdOrderByCreatedAtDesc(resume.getResumeId());
            if (!versions.isEmpty()) {
                previousScore = versions.get(0).getAtsScore();
                if (previousScore == null) previousScore = 0;
            }
        }

        // 2. Initial ATS Scoring
        AtsClient.AtsScoreResult result = atsClient.score(
                resumeText,
                jobDescription,
                Collections.emptyList() // Pass an empty list for skills for now
        );

        Integer currentScore = result.getScore();
        if (currentScore == null) currentScore = 0;

        // 3. Enforce score >= previous score rule (only for updates)
        if (latestResume.isPresent() && currentScore < previousScore) {
            throw new ApiException("The new resume has an ATS score (" + currentScore + 
                ") lower than the previous one (" + previousScore + "). Please improve it before uploading.");
        }

        // 4. Auto-improve if score < 60
        if (currentScore < 60 && result.getImprovedResumeText() != null) {
            // The improved text is already in the result from AtsClient (AI Engine handled it)
            // We can return this result to the controller
        }

        return result;
    }
}
