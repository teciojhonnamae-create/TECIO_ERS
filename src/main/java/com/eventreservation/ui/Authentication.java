package com.yourdomain.ui;

import com.yourdomain.dao.UserDao;
import com.yourdomain.model.User;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_.]{4,20}$");

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println(
                    "===== EVENT RESERVATION SYSTEM ====="
            );
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> registerAccount();
                case "2" -> login();
                case "3" -> {
                    System.out.println(
                            "Application closed. Goodbye!"
                    );
                    running = false;
                }
                default -> System.out.println(
                        "Invalid choice. Enter 1, 2, or 3."
                );
            }
        }
    }

    private void registerAccount() {

        System.out.println();
        System.out.println("--- REGISTER ACCOUNT ---");

        String fullName = ask("Full name: ").trim();

        if (fullName.length() < 2
                || fullName.length() > 80) {
            System.out.println(
                    "Full name must be 2 to 80 characters."
            );
            return;
        }

        String username = ask(
                "Username (4-20 letters, numbers, _ or .): "
        ).trim();

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            System.out.println(
                    "Invalid username format."
            );
            return;
        }

        String password = ask(
                "Password (at least 8 characters): "
        );

        if (password.length() < 8) {
            System.out.println(
                    "Password must be at least 8 characters."
            );
            return;
        }

        String confirmPassword =
                ask("Confirm password: ");

        if (!password.equals(confirmPassword)) {
            System.out.println(
                    "Passwords do not match."
            );
            return;
        }

        System.out.println();
        System.out.println("Choose your role:");
        System.out.println("1. ADMIN");
        System.out.println("2. CASHIER");
        System.out.println("3. EVENT COORDINATOR");

        String roleChoice = ask("Role: ").trim();

        String role = switch (roleChoice) {
            case "1" -> "ADMIN";
            case "2" -> "CASHIER";
            case "3" -> "EVENT COORDINATOR";
            default -> null;
        };

        if (role == null) {
            System.out.println("Invalid role selection.");
            return;
        }

        try {
            if (userDao.usernameExists(username)) {
                System.out.println(
                        "Registration failed: username already exists."
                );
                return;
            }

            userDao.register(
                    fullName,
                    username,
                    password,
                    role
            );

            System.out.println();
            System.out.println("Registration successful!");
            System.out.println("Assigned role: " + role);
            System.out.println("Account status: PENDING");
            System.out.println(
                    "Please wait for an administrator to approve "
                            + "your account before logging in."
            );

        } catch (SQLException e) {
            System.out.println(
                    "Could not register account: " + e.getMessage()
            );
        }
    }

    private void login() {

        System.out.println();
        System.out.println("--- LOGIN ---");

        String username = ask("Username: ").trim();
        String password = ask("Password: ");

        if (username.isBlank() || password.isBlank()) {
            System.out.println(
                    "Username and password are required."
            );
            return;
        }

        try {
            Optional<User> result =
                    userDao.login(username, password);

            if (result.isEmpty()) {
                System.out.println(
                        "Login failed: incorrect username or password."
                );
                return;
            }

            User currentUser = result.get();

            if (!"APPROVED".equalsIgnoreCase(
                    currentUser.accountStatus())) {

                System.out.println(
                        "Login denied: your account status is "
                                + currentUser.accountStatus() + "."
                );

                System.out.println(
                        "Please contact the system administrator."
                );
                return;
            }

            System.out.println();
            System.out.println("Login successful!");
            System.out.println(
                    "Welcome, " + currentUser.fullName() + "!"
            );
            System.out.println(
                    "Role: " + currentUser.userRole()
            );

            routeUser(currentUser);

        } catch (SQLException e) {
            System.out.println(
                    "Could not verify login details: "
                            + e.getMessage()
            );
        }
    }

    private void routeUser(User user) {

        switch (user.userRole()) {

            case "ADMIN" -> new AdminMenu(
                    userDao, scanner
            ).start();

            case "CASHIER" -> new CashierMenu(
                    user, scanner
            ).start();

            case "EVENT COORDINATOR" ->
                    new EventCoordinatorMenu(
                            user, scanner
                    ).start();

            default -> System.out.println(
                    "Error: unsupported user role."
            );
        }
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}