
package com.yourdomain.ui;

import com.yourdomain.dao.UserDao;
import com.yourdomain.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentAdmin;

    // Constructor
    public AdminMenu(
            UserDao userDao,
            Scanner scanner,
            User currentAdmin
    ) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    // Main admin menu
    public void start() {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("===== ADMIN MENU =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> viewAllUsers();

                case "2" -> viewPendingUsers();

                case "3" -> approveUserAccount();

                case "4" -> updateUser();

                case "5" -> {
                    System.out.println("Admin logged out.");
                    running = false;
                }

                default -> System.out.println(
                        "Invalid choice. Enter 1, 2, 3, 4, or 5."
                );
            }
        }
    }

    // Display all registered users.
    private void viewAllUsers() {

        try {

            List<User> users = userDao.getAllUsers();

            System.out.println();
            System.out.println("===== ALL SYSTEM USERS =====");

            displayUsers(users);

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve users: " + e.getMessage()
            );
        }
    }

    // Display pending registrations.
    private void viewPendingUsers() {

        try {

            List<User> users = userDao.getPendingUsers();

            System.out.println();
            System.out.println("===== PENDING REGISTRATIONS =====");

            displayUsers(users);

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve pending users: "
                            + e.getMessage()
            );
        }
    }

    // Approve a pending account.
    private void approveUserAccount() {

        try {

            List<User> pendingUsers =
                    userDao.getPendingUsers();

            System.out.println();
            System.out.println("===== APPROVE USER ACCOUNT =====");

            if (pendingUsers.isEmpty()) {

                System.out.println(
                        "There are no pending registrations."
                );

                return;
            }

            displayUsers(pendingUsers);

            System.out.println("Enter 0 to cancel.");
            int targetUserId = readIntInput(
                    "Enter Target User ID: "
            );

            if (targetUserId == 0) {
                System.out.println("Approval cancelled.");
                return;
            }

            if (targetUserId < 1) {
                System.out.println("Invalid user ID.");
                return;
            }

            boolean isPending = pendingUsers.stream()
                    .anyMatch(user ->
                            user.userId() == targetUserId
                    );

            if (!isPending) {

                System.out.println(
                        "Approval failed: User ID not found "
                                + "in pending registrations."
                );

                return;
            }

            boolean approved =
                    userDao.approveUser(targetUserId);

            if (approved) {

                System.out.println(
                        "Account approved successfully!"
                );

                System.out.println(
                        "User ID: " + targetUserId
                );

                System.out.println(
                        "New status: APPROVED"
                );

            } else {

                System.out.println(
                        "Approval failed. The account may "
                                + "already have been processed."
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not approve account: " + e.getMessage()
            );
        }
    }

    // NEW FEATURE:
    // Update another user's role and account status.
    private void updateUser() {

        try {

            System.out.println();
            System.out.println("===== UPDATE USER =====");

            // Step 1: Display all registered users.
            List<User> users = userDao.getAllUsers();

            if (users.isEmpty()) {

                System.out.println(
                        "No registered users found."
                );

                return;
            }

            displayUsers(users);

            System.out.println(
                    "Enter 0 at any step to cancel the update."
            );

            // Step 2: Ask for the target user ID.
            int targetUserId = readIntInput(
                    "Enter Target User ID: "
            );

            if (targetUserId == 0) {
                System.out.println("Update cancelled.");
                return;
            }

            if (targetUserId < 1) {
                System.out.println("Invalid user ID.");
                return;
            }

            // Step 3: Check whether the user exists.
            User targetUser = users.stream()
                    .filter(user ->
                            user.userId() == targetUserId
                    )
                    .findFirst()
                    .orElse(null);

            if (targetUser == null) {

                System.out.println(
                        "User ID not found. Update cancelled."
                );

                return;
            }

            // Step 4: Security restriction.
            // An admin cannot modify their own account.
            if (targetUserId == currentAdmin.userId()) {

                System.out.println();
                System.out.println(
                        "WARNING: Modifying the currently active "
                                + "admin account is restricted."
                );

                System.out.println(
                        "You cannot update your own account."
                );

                return;
            }

            // Step 5: Ask for the new role.
            System.out.println();
            System.out.println(
                    "Current Role: " + targetUser.userRole()
            );

            String newRole = readValidRole();

            if (newRole == null) {
                System.out.println("Update cancelled.");
                return;
            }

            // Step 6: Ask for the new account status.
            System.out.println();
            System.out.println(
                    "Current Status: " + targetUser.accountStatus()
            );

            String newStatus = readValidStatus();

            if (newStatus == null) {
                System.out.println("Update cancelled.");
                return;
            }

            // Step 7: Confirm the update.
            System.out.println();
            System.out.println("===== UPDATE SUMMARY =====");
            System.out.println("User ID: " + targetUserId);
            System.out.println("Username: " + targetUser.username());
            System.out.println(
                    "Old Role: " + targetUser.userRole()
            );
            System.out.println("New Role: " + newRole);
            System.out.println(
                    "Old Status: " + targetUser.accountStatus()
            );
            System.out.println("New Status: " + newStatus);

            // Step 8: Update the database.
            boolean updated =
                    userDao.updateUserRoleAndStatus(
                            targetUserId,
                            newRole,
                            newStatus
                    );

            if (updated) {

                System.out.println();
                System.out.println(
                        "======================================"
                );
                System.out.println(
                        "USER UPDATED SUCCESSFULLY!"
                );
                System.out.println(
                        "======================================"
                );

                System.out.println(
                        "Updated User ID: " + targetUserId
                );

                System.out.println(
                        "Updated Role: " + newRole
                );

                System.out.println(
                        "Updated Status: " + newStatus
                );

            } else {

                System.out.println(
                        "Update failed. User record was not found."
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Could not update user: " + e.getMessage()
            );
        }
    }

    // INPUT VALIDATION WRAPPER 1:
    // Accept only numeric input.
    private int readIntInput(String prompt) {

        while (true) {

            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            try {

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid input! Please enter numbers only."
                );
            }
        }
    }

    // INPUT VALIDATION WRAPPER 2:
    // Accept only existing system roles.
    private String readValidRole() {

        while (true) {

            System.out.println();
            System.out.println("Available Roles:");
            System.out.println("1. ADMIN");
            System.out.println("2. CASHIER");
            System.out.println("3. EVENT COORDINATOR");
            System.out.println("0. Cancel");

            System.out.print("Enter New Role: ");

            String input = scanner.nextLine()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (input.equals("0")) {
                return null;
            }

            switch (input) {

                case "1", "ADMIN" -> {
                    return "ADMIN";
                }

                case "2", "CASHIER" -> {
                    return "CASHIER";
                }

                case "3", "EVENT COORDINATOR" -> {
                    return "EVENT COORDINATOR";
                }

                default -> System.out.println(
                        "Invalid role! Choose ADMIN, CASHIER, "
                                + "or EVENT COORDINATOR."
                );
            }
        }
    }

    // INPUT VALIDATION WRAPPER 3:
    // Accept only APPROVED or PENDING.
    private String readValidStatus() {

        while (true) {

            System.out.println();
            System.out.println("Available Account Status:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.println("0. Cancel");

            System.out.print("Enter New Status: ");

            String input = scanner.nextLine()
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (input.equals("0")) {
                return null;
            }

            switch (input) {

                case "1", "APPROVED" -> {
                    return "APPROVED";
                }

                case "2", "PENDING" -> {
                    return "PENDING";
                }

                default -> System.out.println(
                        "Invalid status! Enter APPROVED or PENDING."
                );
            }
        }
    }

    // Display users in a console table.
    private void displayUsers(List<User> users) {

        String border =
                "+------+----------------------+----------------------+----------------------+------------+";

        System.out.println(border);

        System.out.printf(
                "| %-4s | %-20s | %-20s | %-20s | %-10s |%n",
                "ID",
                "Full Name",
                "Username",
                "Role",
                "Status"
        );

        System.out.println(border);

        if (users.isEmpty()) {

            System.out.printf(
                    "| %-84s |%n",
                    "No records found."
            );

        } else {

            for (User user : users) {

                System.out.printf(
                        "| %-4d | %-20.20s | %-20.20s | %-20.20s | %-10.10s |%n",
                        user.userId(),
                        user.fullName(),
                        user.username(),
                        user.userRole(),
                        user.accountStatus()
                );
            }
        }

        System.out.println(border);

        System.out.println(
                "Total records: " + users.size()
        );
    }
}
