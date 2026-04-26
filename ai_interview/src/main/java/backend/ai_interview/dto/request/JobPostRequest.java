package backend.ai_interview.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
@SuppressWarnings("all")
public class JobPostRequest {
 
    private String title;
    
    private String post = "HR";

    @NotBlank(message = "HR Type is required")
    private String hrType;

    private String otherHrType;

    @NotBlank(message = "Working type is required")
    private String workingType;

    private String officeLocation;

    @NotBlank(message = "Start date type is required")
    private String startDateType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate specificStartDate;

    @NotBlank(message = "Salary is required")
    private String salary;

    @NotNull(message = "Last date to apply is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastDateToApply;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotBlank(message = "Skills required is required")
    private String skillsRequired;

    @NotBlank(message = "Who can apply is required")
    private String whoCanApply;
}
