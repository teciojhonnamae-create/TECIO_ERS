package com.yourdomain.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {

    private static final String DB_URL =
            "jdbc:sqlite:students.db";

    private DatabaseConnection() {
    }

    public static Connection getConnection()
            throws SQLException {

        Connection connection =
                DriverManager.getConnection(DB_URL);

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");

            try (var rs = statement.executeQuery(
                    "PRAGMA foreign_keys")) {

                if (!rs.next() || rs.getInt(1) != 1) {
                    throw new SQLException(
                            "Failed to enable SQLite foreign keys."
                    );
                }
            }

            return connection;

        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeException) {
                e.addSuppressed(closeException);
            }

            throw e;
        }
    }

    public static void initializeDatabase()
            throws SQLException {

        DatabaseInitializer.initialize();
    }
}