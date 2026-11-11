package com.yourdomain.ui;

import com.yourdomain.dao.ActivityLogDao;
import com.yourdomain.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class EventCoordinatorMenu {

    private final User user;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;

    public EventCoordinatorMenu(
            User user,
            ActivityLogDao activityLogDao,
            Scanner scanner
    ) {
        this.user = user;
        this.activityLogDao = activityLogDao;
        this.scanner = scanner;
    }

    // Backward-compatible constructor.
    public EventCoordinatorMenu(User user, Scanner scanner) {
        this(user, new ActivityLogDao(), scanner);
    }

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println();
            System.out.println(
                    "===== EVENT COORDINATOR MODULE ====="
            );

            System.out.println("Welcome, " + user.fullName() + "!");

            System.out.println("1. View Module Information");
            System.out.println("2. Logout");

            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> System.out.println(
                        "Event creation and event management "
                                + "features will be added here."
                );

                case "2" -> {

                    recordLogout();

                    System.out.println(
                            "Event Coordinator logged out successfully."
                    );

                    running = false;
                }

                default -> System.out.println(
                        "Invalid choice. Enter 1 or 2."
                );
            }
        }
    }

    private void recordLogout() {

        try {

            activityLogDao.logAction(
                    user.userId(),
                    "USER_LOGOUT",
                    "EVENT COORDINATOR [" + user.userId()
                            + "] logged out of the system."
            );

            System.out.println(
                    "Logout activity saved successfully."
            );

        } catch (SQLException e) {

            System.out.println(
                    "WARNING: Could not save logout activity: "
                            + e.getMessage()
            );
        }
    }
}