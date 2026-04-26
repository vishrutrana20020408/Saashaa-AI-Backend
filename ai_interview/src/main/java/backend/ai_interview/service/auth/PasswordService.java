package backend.ai_interview.service.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Password Service
 *
 * Uses BCrypt hashing algorithm for secure password storage.
 *
 * - Automatically generates salt
 * - Safe against rainbow table attacks
 * - Recommended for production systems
 */
@Service
@SuppressWarnings("all")
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService() {
        // Strength 10 is default and secure
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Hash raw password before saving to database
     *
     * @param rawPassword plain text password
     * @return encrypted password
     */
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Compare raw password with stored encrypted password
     *
     * @param rawPassword     plain text password
     * @param encodedPassword encrypted password from DB
     * @return true if matches
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}