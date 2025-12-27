import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        String adminPassword = "Admin123!";
        String userPassword = "User1234!";
        String managerPassword = "Manager123!";

        System.out.println("=== CORRECT BCrypt Hashes (Java $2a$) ===");
        System.out.println();
        System.out.println("admin (Admin123!):");
        String adminHash = encoder.encode(adminPassword);
        System.out.println(adminHash);
        System.out.println("Verify: " + encoder.matches(adminPassword, adminHash));
        System.out.println();

        System.out.println("user (User1234!):");
        String userHash = encoder.encode(userPassword);
        System.out.println(userHash);
        System.out.println("Verify: " + encoder.matches(userPassword, userHash));
        System.out.println();

        System.out.println("manager (Manager123!):");
        String managerHash = encoder.encode(managerPassword);
        System.out.println(managerHash);
        System.out.println("Verify: " + encoder.matches(managerPassword, managerHash));
        System.out.println();

        // Test with existing hash
        System.out.println("=== Testing existing hash ===");
        String existingHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG";
        System.out.println("Hash: " + existingHash);
        System.out.println("Matches Admin123!: " + encoder.matches("Admin123!", existingHash));
        System.out.println("Matches parking123: " + encoder.matches("parking123", existingHash));
    }
}

