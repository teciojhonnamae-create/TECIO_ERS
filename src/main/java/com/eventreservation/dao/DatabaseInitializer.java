package com.eventreservation.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private DatabaseInitializer() {
        // Prevent object creation.
    }

    public static void initialize() throws SQLException {

        String createUserTable = """
                CREATE TABLE IF NOT EXISTS "user" (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE COLLATE NOCASE,
                    password TEXT NOT NULL,
                    user_role TEXT NOT NULL
                        CHECK (
                            user_role IN (
                                'ADMIN',
                                'CASHIER',
                                'EVENT COORDINATOR'
                            )
                        ),
                    account_status TEXT NOT NULL DEFAULT 'PENDING'
                        CHECK (
                            account_status IN (
                                'PENDING',
                                'ACTIVE',
                                'INACTIVE'
                            )
                        )
                )
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement =
                     connection.createStatement()) {

            statement.execute(createUserTable);

            System.out.println(
                    "Database initialization completed successfully."
            );
        }
    }
}