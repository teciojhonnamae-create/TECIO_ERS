package com.eventreservation.ui;

import com.eventreservation.dao.UserDao;
import com.eventreservation.model.User;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_.]{4,20}$");

    public Authentication(
            UserDao userDao,
            Scanner scanner
    ) {
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
                    "Validation error: full name must be "
                            + "2 to 80 characters."
            );
            return;
        }

        String username = ask(
                "Username (4-20 letters, numbers, _ or .): "
        ).trim();

        if (!USERNAME_PATTERN.matcher(username).matches()) {

            System.out.println(
                    "Validation error: username format is invalid."
            );
            return;
        }

        String password = ask(
                "Password (at least 8 characters): "
        );

        if (password.length() < 8) {

            System.out.println(
                    "Validation error: password must be "
                            + "at least 8 characters."
            );
            return;
        }

        String confirmPassword =
                ask("Confirm password: ");

        if (!password.equals(confirmPassword)) {

            System.out.println(
                    "Validation error: passwords do not match."
            );
            return;
        }

        System.out.println();
        System.out.println("Choose your role:");
        System.out.println("1. ADMIN");
        System.out.println("2. CASHIER");
        System.out.println("3. EVENT COORDINATOR");

        String roleChoice = ask("Role: ").trim();

        String role;

        switch (roleChoice) {

            case "1" -> role = "ADMIN";

            case "2" -> role = "CASHIER";

            case "3" -> role = "EVENT COORDINATOR";

            default -> {
                System.out.println(
                        "Validation error: invalid role selection."
                );
                return;
            }
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
            System.out.println(
                    "Registration successful!"
            );

            System.out.println(
                    "Assigned role: " + role
            );

            System.out.println(
                    "Account status: PENDING"
            );

            System.out.println(
                    "Please wait for your account to be activated."
            );

        } catch (SQLException e) {

            System.out.println(
                    "Could not register account: "
                            + e.getMessage()
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
                    "Validation error: username and password "
                            + "are required."
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

            if (!"ACTIVE".equalsIgnoreCase(
                    currentUser.accountStatus()
            )) {

                System.out.println(
                        "Login denied: your account status is "
                                + currentUser.accountStatus()
                                + "."
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

            case "ADMIN" -> {
                System.out.println();
                System.out.println("===== ADMIN MODULE =====");
                System.out.println(
                        "Welcome to the Admin module."
                );
                System.out.println(
                        "Admin features will be implemented next."
                );
            }

            case "CASHIER" -> {
                System.out.println();
                System.out.println("===== CASHIER MODULE =====");
                System.out.println(
                        "Welcome to the Cashier module."
                );
                System.out.println(
                        "Cashier features will be implemented next."
                );
            }

            case "EVENT COORDINATOR" -> {
                System.out.println();
                System.out.println(
                        "===== EVENT COORDINATOR MODULE ====="
                );
                System.out.println(
                        "Welcome to the Event Coordinator module."
                );
                System.out.println(
                        "Event Coordinator features "
                                + "will be implemented next."
                );
            }

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