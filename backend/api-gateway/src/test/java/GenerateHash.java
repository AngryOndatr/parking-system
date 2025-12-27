import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder10 = new BCryptPasswordEncoder(10);
        BCryptPasswordEncoder encoder12 = new BCryptPasswordEncoder(12);

        String password = "parking123";

        System.out.println("Password: " + password);
        System.out.println("BCrypt(10): " + encoder10.encode(password));
        System.out.println("BCrypt(12): " + encoder12.encode(password));

        // Test existing hash
        String existingHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG";
        System.out.println("\nTesting existing hash: " + existingHash);
        System.out.println("Matches with BCrypt(10): " + encoder10.matches(password, existingHash));
        System.out.println("Matches with BCrypt(12): " + encoder12.matches(password, existingHash));
    }
}

