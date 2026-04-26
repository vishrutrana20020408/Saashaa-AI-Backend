package backend.ai_interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@SuppressWarnings("all")
public class AdminJobApplyRequest {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    @NotBlank(message = "Resume type is required")
    private String resumeType; // WEBSITE, UPLOADED

    private String resumeFileId; // Optional, if UPLOADED
}
