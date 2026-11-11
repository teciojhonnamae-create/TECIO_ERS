package com.yourdomain;

import com.yourdomain.dao.DatabaseInitializer;
import com.yourdomain.dao.UserDao;
import com.yourdomain.dao.ActivityLogDao;
import com.yourdomain.ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {

            // Step 1: Initialize the database.
            DatabaseInitializer.initialize();

            // Step 2: Create DAO objects.
            UserDao userDao = new UserDao();

            ActivityLogDao activityLogDao =
                    new ActivityLogDao();

            // Step 3: Start authentication.
            try (Scanner scanner =
                         new Scanner(System.in)) {

                Authentication authentication =
                        new Authentication(
                                userDao,
                                activityLogDao,
                                scanner
                        );

                authentication.start();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Could not start the application."
            );

            System.err.println(
                    "Check students.db and its schema."
            );

            System.err.println(
                    "Details: " + e.getMessage()
            );
        }
    }
}