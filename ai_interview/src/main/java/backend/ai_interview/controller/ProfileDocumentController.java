package backend.ai_interview.controller;

import backend.ai_interview.exception.ApiException;
import backend.ai_interview.service.profile.ProfileDocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile/documents")
@SuppressWarnings("all")
public class ProfileDocumentController {

    private final ProfileDocumentService profileDocumentService;

    public ProfileDocumentController(ProfileDocumentService profileDocumentService) {
        this.profileDocumentService = profileDocumentService;
    }

    @GetMapping("/view/{role}/{userId}/{docType}")
    public ResponseEntity<byte[]> viewDocument(
            @PathVariable String role,
            @PathVariable String userId,
            @PathVariable String docType,
            Authentication authentication
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }

            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // Secure access: only owner or admin can access
            if (!isAdmin && !currentUsername.equals(userId)) {
                return ResponseEntity.status(403).build();
            }

            byte[] content = profileDocumentService.fetchDocument(role, userId, docType);
            String extension = profileDocumentService.getDocumentExtension(role, userId, docType);
            
            MediaType mediaType = MediaType.APPLICATION_PDF;
            if ("doc".equalsIgnoreCase(extension) || "docx".equalsIgnoreCase(extension)) {
                mediaType = MediaType.valueOf("application/msword");
            } else if ("png".equalsIgnoreCase(extension)) {
                mediaType = MediaType.IMAGE_PNG;
            } else if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) {
                mediaType = MediaType.IMAGE_JPEG;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + docType + "." + extension + "\"")
                    .contentType(mediaType)
                    .body(content);
        } catch (ApiException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
