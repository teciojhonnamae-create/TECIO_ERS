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

            // Step 1: Migrate the existing USER table if necessary.
            migrateUserTableIfNecessary(connection);

            // Step 2: Create the USER table.
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

            // Step 3: Create the ACTIVITY_LOGS table.
            String createActivityLogsTable = """
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        log_id INTEGER PRIMARY KEY AUTOINCREMENT,

                        user_id INTEGER NOT NULL,

                        action_type TEXT NOT NULL,

                        description TEXT NOT NULL,

                        timestamp TEXT NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,

                        FOREIGN KEY (user_id)
                            REFERENCES "user"(user_id)
                    )
                    """;

            try (Statement statement =
                         connection.createStatement()) {

                statement.execute(createUserTable);

                statement.execute(createActivityLogsTable);

                // Step 4: Create an index for user ID filtering.
                statement.execute("""
                        CREATE INDEX IF NOT EXISTS
                        idx_activity_logs_user_id
                        ON activity_logs(user_id)
                        """);

                // Step 5: Create an index for timestamp sorting.
                statement.execute("""
                        CREATE INDEX IF NOT EXISTS
                        idx_activity_logs_timestamp
                        ON activity_logs(timestamp)
                        """);
            }

            System.out.println(
                    "Database initialization completed successfully."
            );

            System.out.println(
                    "Activity logging system initialized successfully."
            );
        }
    }

    private static void migrateUserTableIfNecessary(
            Connection connection
    ) throws SQLException {

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

        boolean originalAutoCommit =
                connection.getAutoCommit();

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