package backend.ai_interview.util;

import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * IdGenerator
 *
 * Generates unique IDs for:
 * - Admin_ID
 * - User_ID
 *
 * Ensures IDs do not collide with existing database records.
 */
@Component
@SuppressWarnings("all")
public class IdGenerator {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    public IdGenerator(AdminRepository adminRepository,
                       UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    /**
     * Generate unique Admin_ID
     */
    public String generateAdminId() {
        String id;

        do {
            id = UUID.randomUUID().toString();
        } while (adminRepository.existsByAdminId(id));

        return id;
    }

    /**
     * Generate unique User_ID
     */
    public String generateUserId() {
        String id;

        do {
            id = UUID.randomUUID().toString();
        } while (userRepository.existsByUserId(id));

        return id;
    }
}