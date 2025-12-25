package com.parking.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        // Production passwords - minimum 8 chars with complexity
        String adminPassword = "Admin123!";
        String userPassword = "User1234!";
        String managerPassword = "Manager123!";

        System.out.println("=== BCrypt Password Hashes (Java $2a$) ===");
        System.out.println();
        System.out.println("Admin password: " + adminPassword);
        System.out.println("Hash: " + encoder.encode(adminPassword));
        System.out.println();
        System.out.println("User password: " + userPassword);
        System.out.println("Hash: " + encoder.encode(userPassword));
        System.out.println();
        System.out.println("Manager password: " + managerPassword);
        System.out.println("Hash: " + encoder.encode(managerPassword));
        System.out.println();
        System.out.println("=== Verification ===");
        String adminHash = encoder.encode(adminPassword);
        System.out.println("Admin verification: " + encoder.matches(adminPassword, adminHash));
    }
}

