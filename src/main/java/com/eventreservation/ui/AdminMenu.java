package com.yourdomain.ui;

import com.yourdomain.dao.UserDao;
import com.yourdomain.dao.ActivityLogDao;
import com.yourdomain.model.User;
import com.yourdomain.model.ActivityLog;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(
            UserDao userDao,
            ActivityLogDao activityLogDao,
            Scanner scanner,
            User currentAdmin
    ) {
        this.userDao = userDao;
        this.activityLogDao = activityLogDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    // ==========================================
    // MAIN ADMIN MENU
    // ==========================================

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
            System.out.println("6. View System Audit Logs");

            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> viewAllUsers();

                case "2" -> viewPendingUsers();

                case "3" -> approveUserAccount();

                case "4" -> updateUser();

                case "5" -> {
                    recordLogout();

                    System.out.println(
                            "Admin logged out successfully."
                    );

                    running = false;
                }

                case "6" -> auditLogMenu();

                default -> System.out.println(
                        "Invalid choice. Enter 1, 2, 3, 4, 5, or 6."
                );
            }
        }
    }

    // ==========================================
    // VIEW ALL USERS
    // ==========================================

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

    // ==========================================
    // VIEW PENDING USERS
    // ==========================================

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

    // ==========================================
    // APPROVE USER ACCOUNT
    // ==========================================

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
                        "User ID not found in pending registrations."
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

                recordAuditAction(
                        "USER_APPROVED",
                        "Admin [" + currentAdmin.userId()
                                + "] approved account for User ID "
                                + targetUserId + "."
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

    // ==========================================
    // UPDATE USER ACCOUNT
    // ==========================================

    private void updateUser() {

        try {

            System.out.println();
            System.out.println("===== UPDATE USER =====");

            List<User> users = userDao.getAllUsers();

            if (users.isEmpty()) {

                System.out.println(
                        "No registered users found."
                );

                return;
            }

            displayUsers(users);

            System.out.println(
                    "Enter 0 to cancel the update."
            );

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

            // Admin can now update their own account.
            if (targetUserId == currentAdmin.userId()) {

                System.out.println();
                System.out.println(
                        "You are updating your own administrator account."
                );
            }

            System.out.println();

            System.out.println(
                    "Current Role: " + targetUser.userRole()
            );

            String newRole = readValidRole();

            if (newRole == null) {

                System.out.println("Update cancelled.");

                return;
            }

            System.out.println();

            System.out.println(
                    "Current Status: " + targetUser.accountStatus()
            );

            String newStatus = readValidStatus();

            if (newStatus == null) {

                System.out.println("Update cancelled.");

                return;
            }

            // Display update summary.
            System.out.println();
            System.out.println("===== UPDATE SUMMARY =====");

            System.out.println(
                    "User ID: " + targetUserId
            );

            System.out.println(
                    "Username: " + targetUser.username()
            );

            System.out.println(
                    "Old Role: " + targetUser.userRole()
            );

            System.out.println(
                    "New Role: " + newRole
            );

            System.out.println(
                    "Old Status: " + targetUser.accountStatus()
            );

            System.out.println(
                    "New Status: " + newStatus
            );

            System.out.print(
                    "Confirm update? (Y/N): "
            );

            String confirmation =
                    scanner.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("Y")) {

                System.out.println("Update cancelled.");

                return;
            }

            // SAVE CHANGES TO DATABASE.
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

                // RECORD THE SUCCESSFUL UPDATE.
                recordAuditAction(
                        "USER_UPDATED",
                        "Admin [" + currentAdmin.userId()
                                + "] updated User ID "
                                + targetUserId
                                + ": Role "
                                + targetUser.userRole()
                                + " -> " + newRole
                                + ", Status "
                                + targetUser.accountStatus()
                                + " -> " + newStatus + "."
                );

                if (targetUserId == currentAdmin.userId()) {

                    System.out.println();

                    System.out.println(
                            "NOTICE: Your current session remains "
                                    + "active until you log out."
                    );

                    System.out.println(
                            "Your new role/status will be checked "
                                    + "again during your next login."
                    );
                }

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

    // ==========================================
    // AUDIT LOG MENU
    // ==========================================

    private void auditLogMenu() {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println(
                    "===== SYSTEM AUDIT LOG MENU ====="
            );

            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Back to Admin Menu");

            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> viewAllSystemLogs();

                case "2" -> filterLogsByUserId();

                case "3" -> clearHistoricalLogs();

                case "4" -> running = false;

                default -> System.out.println(
                        "Invalid choice. Enter 1, 2, 3, or 4."
                );
            }
        }
    }

    // ==========================================
    // VIEW ALL LOGS
    // ==========================================

    private void viewAllSystemLogs() {

        try {

            List<ActivityLog> logs =
                    activityLogDao.getAllLogs();

            System.out.println();
            System.out.println("===== ALL SYSTEM AUDIT LOGS =====");

            displayLogs(logs);

        } catch (SQLException e) {

            System.out.println(
                    "Could not retrieve logs: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // FILTER LOGS BY USER ID
    // ==========================================

    private void filterLogsByUserId() {

        try {

            System.out.println();
            System.out.println("===== FILTER LOGS BY USER ID =====");

            System.out.println("Enter 0 to cancel.");

            int userId = readIntInput(
                    "Enter User ID: "
            );

            if (userId == 0) {

                System.out.println("Filtering cancelled.");

                return;
            }

            if (userId < 1) {

                System.out.println("Invalid User ID.");

                return;
            }

            List<ActivityLog> logs =
                    activityLogDao.getLogsByUserId(userId);

            System.out.println();

            System.out.println(
                    "===== AUDIT LOGS FOR USER ID: "
                            + userId + " ====="
            );

            displayLogs(logs);

        } catch (SQLException e) {

            System.out.println(
                    "Could not filter logs: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // CLEAR HISTORICAL LOGS
    // ==========================================

    private void clearHistoricalLogs() {

        System.out.println();

        System.out.println(
                "WARNING: This will delete historical audit logs."
        );

        System.out.println(
                "The new LOGS_CLEARED record will be retained."
        );

        System.out.print(
                "Continue? (Y/N): "
        );

        String confirmation =
                scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {

            System.out.println("Operation cancelled.");

            return;
        }

        try {

            int deletedCount =
                    activityLogDao.clearAllLogs(
                            currentAdmin.userId()
                    );

            System.out.println(
                    "Historical logs cleared successfully."
            );

            System.out.println(
                    "Deleted records: " + deletedCount
            );

            System.out.println(
                    "LOGS_CLEARED record saved."
            );

        } catch (SQLException e) {

            System.out.println(
                    "Could not clear logs: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // RECORD AUDIT ACTION
    // ==========================================

    private void recordAuditAction(
            String actionType,
            String description
    ) {

        try {

            activityLogDao.logAction(
                    currentAdmin.userId(),
                    actionType,
                    description
            );

            System.out.println(
                    "Activity recorded in system audit logs."
            );

        } catch (SQLException e) {

            System.out.println(
                    "WARNING: Could not save audit log: "
                            + e.getMessage()
            );
        }
    }

    // ==========================================
    // RECORD ADMIN LOGOUT
    // ==========================================

    private void recordLogout() {

        recordAuditAction(
                "USER_LOGOUT",
                "ADMIN [" + currentAdmin.userId()
                        + "] logged out of the system."
        );
    }

    // ==========================================
    // DISPLAY AUDIT LOGS
    // ==========================================

    private void displayLogs(List<ActivityLog> logs) {

        String border =
                "+-------+----------+--------------------+"
                        + "-------------------------------------+"
                        + "----------------------+";

        System.out.println(border);

        System.out.printf(
                "| %-5s | %-8s | %-18s | %-35s | %-20s |%n",
                "Log ID",
                "User ID",
                "Action Type",
                "Description",
                "Timestamp"
        );

        System.out.println(border);

        if (logs.isEmpty()) {

            System.out.printf(
                    "| %-84s |%n",
                    "No activity logs found."
            );

        } else {

            for (ActivityLog log : logs) {

                System.out.printf(
                        "| %-5d | %-8d | %-18.18s | %-35.35s | %-20.20s |%n",
                        log.logId(),
                        log.userId(),
                        log.actionType(),
                        log.description(),
                        log.timestamp()
                );
            }
        }

        System.out.println(border);

        System.out.println(
                "Total records: " + logs.size()
        );
    }

    // ==========================================
    // INPUT VALIDATION: INTEGER
    // ==========================================

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

    // ==========================================
    // INPUT VALIDATION: ROLE
    // ==========================================

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
                        "Invalid role selection."
                );
            }
        }
    }

    // ==========================================
    // INPUT VALIDATION: ACCOUNT STATUS
    // ==========================================

    private String readValidStatus() {

        while (true) {

            System.out.println();
            System.out.println("Available Account Status:");

            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.println("3. INACTIVE");
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

                case "3", "INACTIVE" -> {
                    return "INACTIVE";
                }

                default -> System.out.println(
                        "Invalid status selection."
                );
            }
        }
    }

    // ==========================================
    // DISPLAY USERS
    // ==========================================

    private void displayUsers(List<User> users) {

        String border =
                "+------+----------------------+----------------------+"
                        + "----------------------+------------+";

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