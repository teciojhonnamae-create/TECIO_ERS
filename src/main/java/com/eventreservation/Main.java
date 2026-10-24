package com.yourdomain;

import com.yourdomain.dao.DatabaseInitializer;
import com.yourdomain.dao.UserDao;
import com.yourdomain.ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            // Step 1: Initialize or migrate the database.
            DatabaseInitializer.initialize();

            // Step 2: Create the DAO.
            UserDao userDao = new UserDao();

            // Step 3: Start the authentication interface.
            try (Scanner scanner =
                         new Scanner(System.in)) {

                Authentication authentication =
                        new Authentication(userDao, scanner);

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