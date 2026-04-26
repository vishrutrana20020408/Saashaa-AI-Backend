package backend.ai_interview.service.profile;

import backend.ai_interview.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Profile Document Service
 *
 * Handles storage for profile documents like marksheets and resumes.
 * Folders: uploads/{role}_{userId}/
 * Files: {docType}.{extension}
 */
@Service
@SuppressWarnings("all")
public class ProfileDocumentService {

    @Value("${app.storage.root:uploads}")
    private String storageRoot;

    public String storeDocument(String role, String userId, MultipartFile file, String docType) {
        try {
            if (userId == null || userId.isBlank()) {
                throw new ApiException("User ID must not be blank");
            }
            if (role == null || role.isBlank()) {
                role = "user";
            }
            if (docType == null || docType.isBlank()) {
                throw new ApiException("Document type must not be blank");
            }
            if (file == null || file.isEmpty()) {
                throw new ApiException("File is empty or null");
            }

            // Normalize role and userId for folder name
            String folderName = role.toLowerCase() + "_" + userId.replaceAll("[^a-zA-Z0-9_-]", "_");
            Path userPath = Paths.get(storageRoot, folderName);
            Files.createDirectories(userPath);

            // Determine extension
            String originalFilename = file.getOriginalFilename();
            String extension = "pdf"; // Default to pdf
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            }
            
            // Clean docType for filename
            String cleanDocType = docType.toLowerCase().replaceAll("[^a-z0-9]", "");
            String storedFilename = cleanDocType + "." + extension;
            Path filePath = userPath.resolve(storedFilename);

            // Delete old files with same docType but potentially different extension
            try (var stream = Files.list(userPath)) {
                stream.filter(p -> p.getFileName().toString().startsWith(cleanDocType + "."))
                      .forEach(p -> {
                          try {
                              Files.deleteIfExists(p);
                          } catch (IOException e) {
                              // ignore
                          }
                      });
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/api/profile/documents/view/" + role + "/" + userId + "/" + cleanDocType;
        } catch (IOException e) {
            throw new ApiException("Failed to store document: " + e.getMessage());
        }
    }

    public byte[] fetchDocument(String role, String userId, String docType) {
        try {
            String folderName = role.toLowerCase() + "_" + userId.replaceAll("[^a-zA-Z0-9_-]", "_");
            Path userPath = Paths.get(storageRoot, folderName);
            String cleanDocType = docType.toLowerCase().replaceAll("[^a-z0-9]", "");
            
            if (!Files.exists(userPath)) {
                throw new ApiException("No documents found for this user");
            }

            try (var stream = Files.list(userPath)) {
                Path filePath = stream.filter(p -> p.getFileName().toString().startsWith(cleanDocType + "."))
                                     .findFirst()
                                     .orElseThrow(() -> new ApiException("Document not found: " + docType));
                
                return Files.readAllBytes(filePath);
            }
        } catch (IOException e) {
            throw new ApiException("Failed to fetch document: " + e.getMessage());
        }
    }

    public String getDocumentExtension(String role, String userId, String docType) {
        try {
            String folderName = role.toLowerCase() + "_" + userId.replaceAll("[^a-zA-Z0-9_-]", "_");
            Path userPath = Paths.get(storageRoot, folderName);
            String cleanDocType = docType.toLowerCase().replaceAll("[^a-z0-9]", "");
            
            if (!Files.exists(userPath)) return "pdf";

            try (var stream = Files.list(userPath)) {
                Path filePath = stream.filter(p -> p.getFileName().toString().startsWith(cleanDocType + "."))
                                     .findFirst()
                                     .orElse(null);
                
                if (filePath != null) {
                    String filename = filePath.getFileName().toString();
                    if (filename.contains(".")) {
                        return filename.substring(filename.lastIndexOf(".") + 1);
                    }
                }
            }
            return "pdf";
        } catch (IOException e) {
            return "pdf";
        }
    }

    public void deleteDocument(String role, String userId, String docType) {
        try {
            String folderName = role.toLowerCase() + "_" + userId.replaceAll("[^a-zA-Z0-9_-]", "_");
            Path userPath = Paths.get(storageRoot, folderName);
            String cleanDocType = docType.toLowerCase().replaceAll("[^a-z0-9]", "");
            
            if (!Files.exists(userPath)) return;

            try (var stream = Files.list(userPath)) {
                stream.filter(p -> p.getFileName().toString().startsWith(cleanDocType + "."))
                      .forEach(p -> {
                          try {
                              Files.deleteIfExists(p);
                          } catch (IOException e) {
                              // ignore
                          }
                      });
            }
        } catch (IOException e) {
            throw new ApiException("Failed to delete document: " + e.getMessage());
        }
    }
}
