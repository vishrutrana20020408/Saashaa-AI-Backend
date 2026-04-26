package backend.ai_interview.repository;

import backend.ai_interview.entity.ToolRequirementAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Tool Requirement Answer Repository
 *
 * Handles database operations for ToolRequirementAnswer entity
 * in the latest backend-integrated project structure.
 *
 * Relationships:
 * JobApplication (1) -> (N) ToolRequirementAnswer
 */
@Repository
@SuppressWarnings("all")
public interface ToolRequirementAnswerRepository extends JpaRepository<ToolRequirementAnswer, Long> {

    /**
     * Fetch all tool answers for a job application.
     */
    List<ToolRequirementAnswer> findByJobApplication_Id(Long jobApplicationId);

    /**
     * Fetch all tool answers for a job application ordered by creation time ascending.
     */
    List<ToolRequirementAnswer> findByJobApplication_IdOrderByCreatedAtAsc(Long jobApplicationId);

    /**
     * Fetch all tool answers submitted by a specific user
     * through jobApplication -> user.
     */
    List<ToolRequirementAnswer> findByJobApplication_User_UserId(String userId);

    /**
     * Fetch all tool answers for a specific user and job application.
     */
    List<ToolRequirementAnswer> findByJobApplication_IdAndJobApplication_User_UserId(
            Long jobApplicationId,
            String userId
    );

    /**
     * Fetch all tool answers by tool name.
     */
    List<ToolRequirementAnswer> findByToolName(String toolName);

    /**
     * Fetch all required tool answers for a job application.
     */
    List<ToolRequirementAnswer> findByJobApplication_IdAndRequiredTrue(Long jobApplicationId);

    /**
     * Fetch all tool answers where the user knows the tool.
     */
    List<ToolRequirementAnswer> findByJobApplication_IdAndUserKnowsToolTrue(Long jobApplicationId);

    /**
     * Fetch all tool answers for a job application ordered by newest first.
     */
    List<ToolRequirementAnswer> findByJobApplication_IdOrderByCreatedAtDesc(Long jobApplicationId);

    /**
     * Delete all tool answers for a job application.
     */
    void deleteByJobApplication_Id(Long jobApplicationId);

    /**
     * Count tool answers for a job application.
     */
    long countByJobApplication_Id(Long jobApplicationId);
}