package backend.ai_interview.service.resume;

import backend.ai_interview.dto.request.ResumeCreateRequest;
import backend.ai_interview.exception.ResumePdfGenerationException;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ResumePdfGenerationService
 *
 * Generates resume PDFs from structured resume editor data.
 */
@Service
@SuppressWarnings("all")
public class ResumePdfGenerationService {

    /**
     * Generate PDF as byte[]
     */
    public byte[] generatePdfBytes(ResumeCreateRequest request) {
        if (request == null) {
            throw ResumePdfGenerationException.missingContent(null, null);
        }

        try {
            String html = renderResumeHtml(request);
            return renderHtmlToPdfBytes(html);
        } catch (ResumePdfGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ResumePdfGenerationException.generationFailed(null, null, ex);
        }
    }

    /**
     * Generate PDF temp file
     */
    public Path generatePdfTempFile(ResumeCreateRequest request) {
        try {
            byte[] pdfBytes = generatePdfBytes(request);

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw ResumePdfGenerationException.invalidOutput(null, null);
            }

            Path tempFile = Files.createTempFile("resume-", ".pdf");
            Files.write(tempFile, pdfBytes);
            return tempFile;

        } catch (IOException ex) {
            throw ResumePdfGenerationException.exportFailed(null, null, ex);
        }
    }

    /**
     * Render HTML
     */
    public String renderResumeHtml(ResumeCreateRequest request) {
        String resumeName = escapeHtml(safe(request.getResumeName()));
        String summary = escapeHtml(safe(request.getSummary()));

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            font-size: 12px;
                            margin: 20px;
                            color: #222;
                        }
                        h1 {
                            font-size: 20px;
                            margin-bottom: 12px;
                        }
                        h2 {
                            font-size: 14px;
                            margin-top: 16px;
                            margin-bottom: 8px;
                        }
                        p {
                            margin: 0;
                            line-height: 1.5;
                        }
                    </style>
                </head>
                <body>
                    <h1>%s</h1>
                    <h2>Summary</h2>
                    <p>%s</p>
                </body>
                </html>
                """.formatted(resumeName, summary);
    }

    /**
     * Convert HTML to PDF
     */
    private byte[] renderHtmlToPdfBytes(String html) {
        if (html == null || html.isBlank()) {
            throw ResumePdfGenerationException.missingContent(null, null);
        }

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();

            byte[] bytes = os.toByteArray();

            if (bytes.length == 0) {
                throw ResumePdfGenerationException.invalidOutput(null, null);
            }

            return bytes;

        } catch (Exception ex) {
            throw ResumePdfGenerationException.generationFailed(null, null, ex);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}