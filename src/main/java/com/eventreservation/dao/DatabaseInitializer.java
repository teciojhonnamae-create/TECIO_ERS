package com.yourdomain.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            migrateUserTableIfNecessary(connection);

            String createUserTable = """
                    CREATE TABLE IF NOT EXISTS "user" (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        full_name TEXT NOT NULL,
                        username TEXT NOT NULL UNIQUE
                            COLLATE NOCASE,
                        password TEXT NOT NULL,
                        user_role TEXT NOT NULL
                            CHECK (
                                user_role IN (
                                    'ADMIN',
                                    'CASHIER',
                                    'EVENT COORDINATOR'
                                )
                            ),
                        account_status TEXT NOT NULL
                            DEFAULT 'PENDING'
                            CHECK (
                                account_status IN (
                                    'PENDING',
                                    'APPROVED',
                                    'INACTIVE'
                                )
                            )
                    )
                    """;

            try (Statement statement =
                         connection.createStatement()) {

                statement.execute(createUserTable);
            }

            System.out.println(
                    "Database initialization completed successfully."
            );
        }
    }

    private static void migrateUserTableIfNecessary(
            Connection connection) throws SQLException {

        String tableSql = null;

        try (var ps = connection.prepareStatement("""
                SELECT sql
                FROM sqlite_master
                WHERE type = 'table'
                  AND name = 'user'
                """);
             var rs = ps.executeQuery()) {

            if (rs.next()) {
                tableSql = rs.getString("sql");
            }
        }

        // A new database does not need migration.
        if (tableSql == null) {
            return;
        }

        // The existing table already supports APPROVED.
        if (tableSql.toUpperCase().contains("APPROVED")) {
            return;
        }

        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            try (Statement statement =
                         connection.createStatement()) {

                statement.execute("""
                        ALTER TABLE "user"
                        RENAME TO "user_old"
                        """);

                statement.execute("""
                        CREATE TABLE "user" (
                            user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            full_name TEXT NOT NULL,
                            username TEXT NOT NULL UNIQUE
                                COLLATE NOCASE,
                            password TEXT NOT NULL,
                            user_role TEXT NOT NULL
                                CHECK (
                                    user_role IN (
                                        'ADMIN',
                                        'CASHIER',
                                        'EVENT COORDINATOR'
                                    )
                                ),
                            account_status TEXT NOT NULL
                                DEFAULT 'PENDING'
                                CHECK (
                                    account_status IN (
                                        'PENDING',
                                        'APPROVED',
                                        'INACTIVE'
                                    )
                                )
                        )
                        """);

                statement.execute("""
                        INSERT INTO "user" (
                            user_id,
                            full_name,
                            username,
                            password,
                            user_role,
                            account_status
                        )
                        SELECT
                            user_id,
                            full_name,
                            username,
                            password,
                            user_role,
                            CASE
                                WHEN account_status = 'ACTIVE'
                                    THEN 'APPROVED'
                                ELSE account_status
                            END
                        FROM "user_old"
                        """);

                statement.execute("""
                        DROP TABLE "user_old"
                        """);
            }

            connection.commit();

        } catch (SQLException e) {

            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                e.addSuppressed(rollbackException);
            }

            throw e;

        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }
}