package com.yourdomain.ui;

import com.yourdomain.model.User;

import java.util.Scanner;

public class EventCoordinatorMenu {

    private final User user;
    private final Scanner scanner;

    public EventCoordinatorMenu(
            User user,
            Scanner scanner
    ) {
        this.user = user;
        this.scanner = scanner;
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
                    System.out.println(
                            "Event Coordinator logged out."
                    );
                    running = false;
                }
                default -> System.out.println(
                        "Invalid choice. Enter 1 or 2."
                );
            }
        }
    }
}