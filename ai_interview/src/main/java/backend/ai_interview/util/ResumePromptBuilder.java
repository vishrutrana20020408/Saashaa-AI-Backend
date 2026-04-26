package backend.ai_interview.util;

import backend.ai_interview.dto.request.ResumeTailorRequest;
import backend.ai_interview.dto.request.ToolKnowledgeAnswerRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resume Prompt Builder
 *
 * Builds structured prompt text that can later be sent to:
 * - an AI/LLM service
 * - an ATS optimization engine
 * - a tailoring workflow
 *
 * Responsibilities:
 * - build prompt for extracting tools from a job description
 * - build prompt for tailoring a duplicate resume version
 * - build prompt for generating ATS improvement suggestions
 * - build prompt for safe skill inclusion based on user tool answers
 *
 * NOTE:
 * This class only builds prompt strings.
 * It does not call any external AI service directly.
 *
 * Latest project alignment:
 * - supports backend-integrated resume tailoring flow
 * - stays aligned with resume version duplicate/tailoring design
 * - keeps prompts compatible with structured resume content, raw text, and tool-answer flow
 */
@Component
@SuppressWarnings("all")
public class ResumePromptBuilder {

    /**
     * Build prompt for extracting tools/skills/keywords from a job description.
     */
    public String buildToolExtractionPrompt(ResumeTailorRequest request) {
        validateTailorRequest(request);

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert resume and ATS analyzer.\n");
        prompt.append("Extract the important tools, technologies, frameworks, platforms, role keywords, and responsibilities from the following job description.\n\n");

        prompt.append("Return the result in a structured format with these groups:\n");
        prompt.append("1. Required tools\n");
        prompt.append("2. Preferred tools\n");
        prompt.append("3. Core skills\n");
        prompt.append("4. ATS keywords\n");
        prompt.append("5. Role responsibilities\n");
        prompt.append("6. Recommended skills to highlight only if the candidate genuinely knows them\n\n");

        prompt.append("Company: ").append(safe(request.getCompanyName())).append("\n");
        prompt.append("Job Title: ").append(safe(request.getJobTitle())).append("\n");
        prompt.append("Base Resume Version ID: ").append(request.getResumeVersionId()).append("\n\n");

        if (!safe(request.getAdditionalNotes()).isBlank()) {
            prompt.append("Additional Candidate Notes:\n");
            prompt.append(safe(request.getAdditionalNotes())).append("\n\n");
        }

        prompt.append("Job Description:\n");
        prompt.append(safe(request.getJobDescription())).append("\n");

        return prompt.toString();
    }

    /**
     * Build prompt for tailoring resume content to a job description.
     */
    public String buildResumeTailoringPrompt(
            ResumeTailorRequest request,
            Map<String, Object> structuredResumeContent
    ) {
        validateTailorRequest(request);

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert resume writer and ATS optimization assistant.\n");
        prompt.append("Your task is to tailor the resume content for the target job.\n");
        prompt.append("Do not fabricate skills, tools, or experiences the candidate does not have.\n");
        prompt.append("Keep the resume honest, concise, and ATS-friendly.\n");
        prompt.append("This tailoring must produce content suitable for a duplicate resume version, not overwrite the original base resume.\n\n");

        prompt.append("Goals:\n");
        prompt.append("- Improve ATS alignment with the job description\n");
        prompt.append("- Rewrite summary for the target role\n");
        prompt.append("- Reorder and emphasize relevant skills\n");
        prompt.append("- Improve experience/project bullet points using genuine matching tools\n");
        prompt.append("- Exclude unsupported tools that the candidate does not know\n");
        prompt.append("- Preserve the original resume meaning\n");
        prompt.append("- Keep the output consistent with a structured resume editor flow\n\n");

        prompt.append("Company: ").append(safe(request.getCompanyName())).append("\n");
        prompt.append("Job Title: ").append(safe(request.getJobTitle())).append("\n");
        prompt.append("Base Resume Version ID: ").append(request.getResumeVersionId()).append("\n\n");

        prompt.append("Known Tools Confirmed By Candidate:\n");
        prompt.append(formatList(request.getKnownTools())).append("\n\n");

        prompt.append("Unknown Tools / Do Not Claim:\n");
        prompt.append(formatList(request.getUnknownTools())).append("\n\n");

        if (!safe(request.getAdditionalNotes()).isBlank()) {
            prompt.append("Additional Candidate Notes:\n");
            prompt.append(safe(request.getAdditionalNotes())).append("\n\n");
        }

        prompt.append("Current Structured Resume Content:\n");
        prompt.append(prettyPrintMap(structuredResumeContent)).append("\n\n");

        prompt.append("Target Job Description:\n");
        prompt.append(safe(request.getJobDescription())).append("\n\n");

        prompt.append("Return a structured response containing:\n");
        prompt.append("1. Tailored professional summary\n");
        prompt.append("2. Recommended skills ordering\n");
        prompt.append("3. Tailored experience bullets\n");
        prompt.append("4. Tailored project bullets\n");
        prompt.append("5. ATS keyword coverage notes\n");
        prompt.append("6. Unsupported tools excluded from tailoring\n");
        prompt.append("7. Recommended preview-ready content blocks for the tailored duplicate version\n");

        return prompt.toString();
    }

    /**
     * Build prompt for ATS optimization suggestions.
     */
    public String buildAtsOptimizationPrompt(
            String jobDescription,
            Map<String, Object> structuredResumeContent
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an ATS optimization expert.\n");
        prompt.append("Analyze the resume against the target job description and provide improvement suggestions.\n");
        prompt.append("Focus on keyword alignment, clarity, section quality, and recruiter readability.\n");
        prompt.append("Keep suggestions realistic for a resume version editing and preview workflow.\n\n");

        prompt.append("Target Job Description:\n");
        prompt.append(safe(jobDescription)).append("\n\n");

        prompt.append("Resume Content:\n");
        prompt.append(prettyPrintMap(structuredResumeContent)).append("\n\n");

        prompt.append("Return:\n");
        prompt.append("1. Missing keywords\n");
        prompt.append("2. Weak sections\n");
        prompt.append("3. Suggested summary improvements\n");
        prompt.append("4. Suggested skill ordering\n");
        prompt.append("5. Suggested bullet rewrites\n");
        prompt.append("6. Estimated ATS improvement opportunities\n");
        prompt.append("7. Notes for safer tailoring without inventing unsupported skills\n");

        return prompt.toString();
    }

    /**
     * Build prompt from a list of tool knowledge answers.
     */
    public String buildToolKnowledgePrompt(List<ToolKnowledgeAnswerRequest> answers) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are validating which tools can be safely included in a resume.\n");
        prompt.append("Use the candidate's answers below to decide:\n");
        prompt.append("- INCLUDE_CONFIDENTLY\n");
        prompt.append("- INCLUDE_CAUTIOUSLY\n");
        prompt.append("- DO_NOT_INCLUDE\n\n");

        List<ToolKnowledgeAnswerRequest> safeAnswers =
                answers == null ? Collections.emptyList() : answers;

        if (safeAnswers.isEmpty()) {
            prompt.append("No tool knowledge answers were provided.\n");
            return prompt.toString();
        }

        for (int i = 0; i < safeAnswers.size(); i++) {
            ToolKnowledgeAnswerRequest answer = safeAnswers.get(i);
            if (answer == null) {
                continue;
            }

            prompt.append("Tool Answer ").append(i + 1).append(":\n");
            prompt.append("- Tool Name: ").append(safe(answer.getToolName())).append("\n");
            prompt.append("- Required: ").append(answer.getRequired()).append("\n");
            prompt.append("- User Knows Tool: ").append(answer.getUserKnowsTool()).append("\n");
            prompt.append("- Experience Level: ").append(safe(answer.getUserExperienceLevel())).append("\n");
            prompt.append("- Notes: ").append(safe(answer.getNotes())).append("\n\n");
        }

        prompt.append("Return a decision and short reasoning for each tool.\n");
        prompt.append("Do not recommend claiming tools the candidate clearly does not know.\n");
        return prompt.toString();
    }

    /**
     * Build a compact prompt using raw resume text instead of structured content.
     */
    public String buildRawTextTailoringPrompt(
            ResumeTailorRequest request,
            String rawResumeText
    ) {
        validateTailorRequest(request);

        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an ATS resume tailoring assistant.\n");
        prompt.append("Tailor the candidate's resume for the target job while keeping it truthful.\n");
        prompt.append("Do not invent experience or unsupported tools.\n");
        prompt.append("This output is intended for a duplicate resume version, not the original base version.\n\n");

        prompt.append("Company: ").append(safe(request.getCompanyName())).append("\n");
        prompt.append("Job Title: ").append(safe(request.getJobTitle())).append("\n");
        prompt.append("Base Resume Version ID: ").append(request.getResumeVersionId()).append("\n\n");

        prompt.append("Known Tools:\n");
        prompt.append(formatList(request.getKnownTools())).append("\n\n");

        prompt.append("Unknown Tools:\n");
        prompt.append(formatList(request.getUnknownTools())).append("\n\n");

        if (!safe(request.getAdditionalNotes()).isBlank()) {
            prompt.append("Additional Candidate Notes:\n");
            prompt.append(safe(request.getAdditionalNotes())).append("\n\n");
        }

        prompt.append("Candidate Resume Text:\n");
        prompt.append(safe(rawResumeText)).append("\n\n");

        prompt.append("Target Job Description:\n");
        prompt.append(safe(request.getJobDescription())).append("\n\n");

        prompt.append("Return a tailored summary, skills emphasis, and improved bullet points.\n");
        prompt.append("Also mention excluded unsupported tools and ATS keyword alignment notes.\n");

        return prompt.toString();
    }

    /**
     * Build a simple keyword comparison prompt.
     */
    public String buildKeywordGapPrompt(String resumeText, String jobDescription) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Compare the resume and job description below.\n");
        prompt.append("Identify missing keywords, matching keywords, and improvement suggestions.\n");
        prompt.append("Keep the analysis useful for a resume editor and ATS optimization workflow.\n\n");

        prompt.append("Resume Text:\n");
        prompt.append(safe(resumeText)).append("\n\n");

        prompt.append("Job Description:\n");
        prompt.append(safe(jobDescription)).append("\n\n");

        prompt.append("Return:\n");
        prompt.append("- matching keywords\n");
        prompt.append("- missing keywords\n");
        prompt.append("- suggested additions\n");
        prompt.append("- notes on safe inclusion vs unsupported claims\n");

        return prompt.toString();
    }

    private void validateTailorRequest(ResumeTailorRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Resume tailor request cannot be null");
        }
        if (request.getResumeVersionId() == null) {
            throw new IllegalArgumentException("Resume version id is required");
        }
        if (safe(request.getCompanyName()).isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (safe(request.getJobTitle()).isBlank()) {
            throw new IllegalArgumentException("Job title is required");
        }
        if (safe(request.getJobDescription()).isBlank()) {
            throw new IllegalArgumentException("Job description is required");
        }
    }

    private String formatList(List<String> values) {
        List<String> safeValues = values == null
                ? Collections.emptyList()
                : values.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        if (safeValues.isEmpty()) {
            return "- None";
        }

        return safeValues.stream()
                .map(value -> "- " + value)
                .collect(Collectors.joining("\n"));
    }

    private String prettyPrintMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        StringBuilder builder = new StringBuilder();
        appendMap(builder, map, 0);
        return builder.toString().trim();
    }

    @SuppressWarnings("unchecked")
    private void appendMap(StringBuilder builder, Map<String, Object> map, int indent) {
        String indentText = "  ".repeat(Math.max(0, indent));

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            builder.append(indentText)
                    .append(entry.getKey())
                    .append(": ");

            Object value = entry.getValue();

            if (value instanceof Map<?, ?> nestedMap) {
                builder.append("\n");
                appendMap(builder, (Map<String, Object>) nestedMap, indent + 1);
            } else if (value instanceof List<?> list) {
                builder.append("\n");
                appendList(builder, list, indent + 1);
            } else {
                builder.append(safe(String.valueOf(value))).append("\n");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void appendList(StringBuilder builder, List<?> list, int indent) {
        String indentText = "  ".repeat(Math.max(0, indent));

        if (list == null || list.isEmpty()) {
            builder.append(indentText).append("- []\n");
            return;
        }

        for (Object item : list) {
            if (item instanceof Map<?, ?> nestedMap) {
                builder.append(indentText).append("-").append("\n");
                appendMap(builder, (Map<String, Object>) nestedMap, indent + 1);
            } else if (item instanceof List<?> nestedList) {
                builder.append(indentText).append("-").append("\n");
                appendList(builder, nestedList, indent + 1);
            } else {
                builder.append(indentText)
                        .append("- ")
                        .append(safe(String.valueOf(item)))
                        .append("\n");
            }
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}