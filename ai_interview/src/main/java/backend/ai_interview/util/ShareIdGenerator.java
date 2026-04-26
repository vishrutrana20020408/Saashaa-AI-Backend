package backend.ai_interview.util;

import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ShareIdGenerator
 *
 * Generates a unique Share_ID that is guaranteed to not exist in:
 * - Admin table
 * - Users table
 *
 * This satisfies:
 * "Share_ID should be unique for each admin and should not be same as the user database table"
 */
@Component
@SuppressWarnings("all")
public class ShareIdGenerator {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    public ShareIdGenerator(AdminRepository adminRepository, UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generate a Share_ID that is unique across BOTH Admin and Users.
     */
    public String generateUniqueShareId() {
        String shareId;

        do {
            shareId = UUID.randomUUID().toString();
        } while (adminRepository.existsByShareId(shareId) || userRepository.existsByShareId(shareId));

        return shareId;
    }
}