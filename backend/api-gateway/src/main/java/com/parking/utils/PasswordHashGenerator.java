package com.parking.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        // Generate hashes for our users
        String adminPassword = "Admin123!";
        String userPassword = "User123!";
        String managerPassword = "Manager123!";

        System.out.println("=".repeat(60));
        System.out.println("PASSWORD HASH GENERATION");
        System.out.println("=".repeat(60));
        System.out.println();

        String adminHash = encoder.encode(adminPassword);
        System.out.println("Username: admin");
        System.out.println("Password: " + adminPassword);
        System.out.println("Hash:     " + adminHash);
        System.out.println();

        String userHash = encoder.encode(userPassword);
        System.out.println("Username: user");
        System.out.println("Password: " + userPassword);
        System.out.println("Hash:     " + userHash);
        System.out.println();

        String managerHash = encoder.encode(managerPassword);
        System.out.println("Username: manager");
        System.out.println("Password: " + managerPassword);
        System.out.println("Hash:     " + managerHash);
        System.out.println();

        System.out.println("=".repeat(60));
        System.out.println("SQL UPDATE STATEMENTS");
        System.out.println("=".repeat(60));
        System.out.println();

        System.out.println("-- Admin user");
        System.out.println("'admin', '" + adminHash + "',");
        System.out.println();
        System.out.println("-- Regular user");
        System.out.println("'user', '" + userHash + "',");
        System.out.println();
        System.out.println("-- Manager user");
        System.out.println("'manager', '" + managerHash + "',");
        System.out.println();

        // Verify the hashes work
        System.out.println("=".repeat(60));
        System.out.println("VERIFICATION");
        System.out.println("=".repeat(60));
        System.out.println();

        System.out.println("Admin password matches:   " + encoder.matches(adminPassword, adminHash));
        System.out.println("User password matches:    " + encoder.matches(userPassword, userHash));
        System.out.println("Manager password matches: " + encoder.matches(managerPassword, managerHash));
    }
}

