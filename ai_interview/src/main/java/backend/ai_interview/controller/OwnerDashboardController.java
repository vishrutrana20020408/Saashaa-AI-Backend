package backend.ai_interview.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.ai_interview.dto.response.ApiResponse;
import backend.ai_interview.entity.AppUser;
import backend.ai_interview.entity.Admin;
import backend.ai_interview.entity.Company;
import backend.ai_interview.repository.AdminRepository;
import backend.ai_interview.repository.CompanyRepository;
import backend.ai_interview.repository.UserRepository;

@RestController
@SuppressWarnings("all")
@RequestMapping("/api/owner/dashboard")
public class OwnerDashboardController {

    private final CompanyRepository companyRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    public OwnerDashboardController(
            CompanyRepository companyRepository,
            AdminRepository adminRepository,
            UserRepository userRepository
    ) {
        this.companyRepository = companyRepository;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/summary")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalCompanies", companyRepository.count());
        data.put("totalAdmins", adminRepository.count());
        data.put("totalUsers", userRepository.count());
        data.put("lastUpdated", LocalDateTime.now().format(DATE_TIME_FORMATTER));

        return ResponseEntity.ok(ApiResponse.success("Owner dashboard summary fetched", data));
    }

    private static String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    @GetMapping("/companies")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCompanies() {
        List<Map<String, Object>> companies = companyRepository.findAll().stream().map(company -> {
            Map<String, Object> map = new HashMap<>();
            map.put("companyId", safeString(company.getCompanyId()));
            map.put("companyName", safeString(company.getCompanyName()));
            map.put("companyType", safeString(company.getCompanyType()));
            map.put("contactPersonName", safeString(company.getContactPersonName()));
            map.put("email", safeString(company.getEmailAddress()));
            map.put("mobileNumber", safeString(company.getMobileNumber()));

            if (company.getCompanyCreatedDate() != null && company.getCompanyCreatedTime() != null) {
                map.put("registeredAt", safeString(company.getCompanyCreatedDate().toString()) + " " + safeString(company.getCompanyCreatedTime().toString()));
                long liveDays = ChronoUnit.DAYS.between(
                        company.getCompanyCreatedDate().atStartOfDay(),
                        LocalDateTime.now()
                );
                map.put("liveDays", liveDays);
            } else {
                map.put("registeredAt", "Unknown");
                map.put("liveDays", 0L);
            }

            map.put("status", "Active");
            return map;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success("Companies fetched", companies));
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAdmins() {
        List<Map<String, Object>> admins = adminRepository.findAll().stream().map(admin -> {
            Map<String, Object> map = new HashMap<>();
            map.put("adminId", safeString(admin.getAdminId()));
            map.put("fullName", safeString(admin.getName()) + " " + safeString(admin.getSurname()));
            map.put("email", safeString(admin.getEmailAddress()));

            if (admin.getAdminCreatedDate() != null && admin.getAdminCreatedTime() != null) {
                map.put("registeredAt", safeString(admin.getAdminCreatedDate().toString()) + " " + safeString(admin.getAdminCreatedTime().toString()));
            } else {
                map.put("registeredAt", "Unknown");
            }

            map.put("currentCompany", "Not working");
            map.put("status", "Not working");
            return map;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success("Admins fetched", admins));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", safeString(user.getUserId()));
            map.put("fullName", safeString(user.getName()) + " " + safeString(user.getSurname()));
            map.put("email", safeString(user.getEmailAddress()));

            if (user.getUserCreatedDate() != null && user.getUserCreatedTime() != null) {
                map.put("registeredAt", safeString(user.getUserCreatedDate().toString()) + " " + safeString(user.getUserCreatedTime().toString()));
            } else {
                map.put("registeredAt", "Unknown");
            }

            String currentCompany = user.getCurrentCompany();
            map.put("currentCompany", currentCompany == null || currentCompany.isBlank() ? "Not working" : currentCompany);
            map.put("status", currentCompany == null || currentCompany.isBlank() ? "Not working" : "Working");
            return map;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success("Users fetched", users));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics() {
        List<AppUser> allUsers = userRepository.findAll();
        List<Company> allCompanies = companyRepository.findAll();
        List<Admin> allAdmins = adminRepository.findAll();

        // Calculate totals
        int totalUsers = allUsers.size();
        int totalCompanies = allCompanies.size();
        int totalAdmins = allAdmins.size();

        // Calculate active users (users who have completed onboarding and created profile)
        long activeUsers = allUsers.stream()
                .filter(user -> user.isOnboardingDone() && user.isProfileCreated())
                .count();
        int inactiveUsers = totalUsers - (int) activeUsers;

        // Calculate growth percentages (current month vs previous month)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfCurrentMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
        LocalDateTime startOfMonthBeforePrevious = startOfPreviousMonth.minusMonths(1);

        // Users growth
        long currentMonthUsers = allUsers.stream()
                .filter(user -> {
                    LocalDateTime userCreated = LocalDateTime.of(user.getUserCreatedDate(), user.getUserCreatedTime());
                    return userCreated.isAfter(startOfCurrentMonth) || userCreated.isEqual(startOfCurrentMonth);
                })
                .count();

        long previousMonthUsers = allUsers.stream()
                .filter(user -> {
                    LocalDateTime userCreated = LocalDateTime.of(user.getUserCreatedDate(), user.getUserCreatedTime());
                    return userCreated.isAfter(startOfPreviousMonth) && userCreated.isBefore(startOfCurrentMonth);
                })
                .count();

        double userGrowth = previousMonthUsers > 0 ?
                ((double) (currentMonthUsers - previousMonthUsers) / previousMonthUsers) * 100 : 0.0;

        // Companies growth
        long currentMonthCompanies = allCompanies.stream()
                .filter(company -> {
                    LocalDateTime companyCreated = LocalDateTime.of(company.getCompanyCreatedDate(), company.getCompanyCreatedTime());
                    return companyCreated.isAfter(startOfCurrentMonth) || companyCreated.isEqual(startOfCurrentMonth);
                })
                .count();

        long previousMonthCompanies = allCompanies.stream()
                .filter(company -> {
                    LocalDateTime companyCreated = LocalDateTime.of(company.getCompanyCreatedDate(), company.getCompanyCreatedTime());
                    return companyCreated.isAfter(startOfPreviousMonth) && companyCreated.isBefore(startOfCurrentMonth);
                })
                .count();

        double companyGrowth = previousMonthCompanies > 0 ?
                ((double) (currentMonthCompanies - previousMonthCompanies) / previousMonthCompanies) * 100 : 0.0;

        // Admins growth
        long currentMonthAdmins = allAdmins.stream()
                .filter(admin -> {
                    LocalDateTime adminCreated = LocalDateTime.of(admin.getAdminCreatedDate(), admin.getAdminCreatedTime());
                    return adminCreated.isAfter(startOfCurrentMonth) || adminCreated.isEqual(startOfCurrentMonth);
                })
                .count();

        long previousMonthAdmins = allAdmins.stream()
                .filter(admin -> {
                    LocalDateTime adminCreated = LocalDateTime.of(admin.getAdminCreatedDate(), admin.getAdminCreatedTime());
                    return adminCreated.isAfter(startOfPreviousMonth) && adminCreated.isBefore(startOfCurrentMonth);
                })
                .count();

        double adminGrowth = previousMonthAdmins > 0 ?
                ((double) (currentMonthAdmins - previousMonthAdmins) / previousMonthAdmins) * 100 : 0.0;

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalUsers", totalUsers);
        analytics.put("totalCompanies", totalCompanies);
        analytics.put("totalAdmins", totalAdmins);
        analytics.put("activeUsers", (int) activeUsers);
        analytics.put("inactiveUsers", inactiveUsers);
        analytics.put("userGrowth", Math.round(userGrowth * 100.0) / 100.0);
        analytics.put("companyGrowth", Math.round(companyGrowth * 100.0) / 100.0);
        analytics.put("adminGrowth", Math.round(adminGrowth * 100.0) / 100.0);
        analytics.put("lastUpdated", LocalDateTime.now().format(DATE_TIME_FORMATTER));

        return ResponseEntity.ok(ApiResponse.success("Analytics data fetched successfully", analytics));
    }
}
