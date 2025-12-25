import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswords {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        // Хэши из базы данных
        String adminHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG";
        String userHash = "$2a$10$WZBJOo6VkVqSN6S9JWtb6upEeF8VqAGsKuGPEv.9M8VpEhZFqiZFO";
        String managerHash = "$2a$10$dXJ3SW6G7P8o0dC/5iGz8OoKmqx5eoo0J1svT2Em.b83TK0Mq/D7K";

        // Пароли для проверки
        String[] passwords = {"parking123", "user123", "manager123", "admin", "admin123", "password", "password123"};

        System.out.println("Testing ADMIN hash:");
        for (String pwd : passwords) {
            if (encoder.matches(pwd, adminHash)) {
                System.out.println("  ✓ MATCH: " + pwd);
            }
        }

        System.out.println("\nTesting USER hash:");
        for (String pwd : passwords) {
            if (encoder.matches(pwd, userHash)) {
                System.out.println("  ✓ MATCH: " + pwd);
            }
        }

        System.out.println("\nTesting MANAGER hash:");
        for (String pwd : passwords) {
            if (encoder.matches(pwd, managerHash)) {
                System.out.println("  ✓ MATCH: " + pwd);
            }
        }

        // Создаём новые правильные хэши
        System.out.println("\n=== Creating NEW hashes ===");
        System.out.println("parking123: " + encoder.encode("parking123"));
        System.out.println("user123: " + encoder.encode("user123"));
        System.out.println("manager123: " + encoder.encode("manager123"));
    }
}

