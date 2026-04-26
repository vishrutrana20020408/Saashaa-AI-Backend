package backend.ai_interview.security;

import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserDetailsService Implementation
 *
 * NOTE:
 * - Not required for JWT-only authentication (your JwtAuthFilter sets Authentication directly)
 * - Useful if you later want to use Spring Security AuthenticationManager/login providers.
 *
 * Behavior:
 * - Try find by email in Admin table first
 * - Else try find by email in Users table
 */
@Service
@SuppressWarnings("all")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(AdminRepository adminRepository, UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1) Try Admin first
        Admin admin = adminRepository.findByEmailAddress(email).orElse(null);
        if (admin != null) {
            return new User(
                    admin.getEmailAddress(),
                    admin.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + Roles.ADMIN))
            );
        }

        // 2) Then try User table
        AppUser user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new UsernameNotFoundException("No admin/user found with email: " + email));

        return new User(
                user.getEmailAddress(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + Roles.USER))
        );
    }
}