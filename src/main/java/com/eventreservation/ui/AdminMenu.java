package com.yourdomain.ui;

import com.yourdomain.dao.UserDao;
import com.yourdomain.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println("===== ADMIN MENU =====");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUserAccount();
                case "4" -> {
                    System.out.println("Admin logged out.");
                    running = false;
                }
                default -> System.out.println(
                        "Invalid choice. Enter 1, 2, 3, or 4."
                );
            }
        }
    }

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

            System.out.println(
                    "Enter 0 to cancel the approval process."
            );
            System.out.print("Enter Target User ID: ");

            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                System.out.println(
                        "Approval cancelled."
                );
                return;
            }

            final int targetUserId;

            try {
                targetUserId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Please enter a valid user ID."
                );
                return;
            }

            if (targetUserId < 1) {
                System.out.println(
                        "Invalid user ID."
                );
                return;
            }

            boolean isPending = pendingUsers.stream()
                    .anyMatch(user ->
                            user.userId() == targetUserId
                    );

            if (!isPending) {
                System.out.println(
                        "Approval failed: the ID is not in the "
                                + "pending registrations list."
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
                        "Approval failed. The account may have "
                                + "already been processed."
                );
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not approve account: " + e.getMessage()
            );
        }
    }

    /**
     * Displays any supplied user list as an aligned console table.
     */
    private void displayUsers(List<User> users) {

        String border =
                "+-------+-----------------+----------------------+------------+";

        System.out.println(border);

        System.out.printf(
                "| %-5s | %-15s | %-20s | %-10s |%n",
                "ID",
                "Username",
                "Role",
                "Status"
        );

        System.out.println(border);

        if (users.isEmpty()) {
            System.out.printf(
                    "| %-65s |%n",
                    "No records found."
            );
        } else {
            for (User user : users) {

                System.out.printf(
                        "| %-5s | %-15s | %-20s | %-10s |%n",
                        user.userId(),
                        user.username(),
                        user.userRole(),
                        user.accountStatus()
                );
            }
        }

        System.out.println(border);
        System.out.println("Total records: " + users.size());
    }
}