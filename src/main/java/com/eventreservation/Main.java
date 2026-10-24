package com.eventreservation;

import com.eventreservation.dao.DatabaseInitializer;
import com.eventreservation.dao.UserDao;
import com.eventreservation.ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {

            // Step 1: Initialize the database.
            DatabaseInitializer.initialize();

            // Step 2: Create shared application resources.
            UserDao userDao = new UserDao();

            try (Scanner scanner = new Scanner(System.in)) {

                // Step 3: Delegate to Authentication.
                Authentication authentication =
                        new Authentication(userDao, scanner);

                authentication.start();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Could not start the application."
            );

            System.err.println(
                    "Check EventReservation.db and its schema."
            );

            System.err.println(
                    "Details: " + e.getMessage()
            );
        }
    }
}