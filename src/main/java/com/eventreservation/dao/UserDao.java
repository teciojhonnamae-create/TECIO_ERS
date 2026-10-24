package com.eventreservation.dao;

import com.eventreservation.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDao {

    /**
     * Checks whether a username already exists.
     */
    public boolean usernameExists(String username)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM "user"
                WHERE username = ? COLLATE NOCASE
                LIMIT 1
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Registers a new user with PENDING account status.
     */
    public void register(
            String fullName,
            String username,
            String password,
            String role
    ) throws SQLException {

        if (!isValidRole(role)) {
            throw new IllegalArgumentException(
                    "Invalid role. Allowed roles: "
                            + "ADMIN, CASHIER, EVENT COORDINATOR."
            );
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        String sql = """
                INSERT INTO "user"
                    (
                        full_name,
                        username,
                        password,
                        user_role,
                        account_status
                    )
                VALUES (?, ?, ?, ?, 'PENDING')
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, role);

            ps.executeUpdate();
        }
    }

    /**
     * Authenticates a user using the stored username and password.
     */
    public Optional<User> login(
            String username,
            String password
    ) throws SQLException {

        String sql = """
                SELECT
                    user_id,
                    full_name,
                    username,
                    user_role,
                    account_status
                FROM "user"
                WHERE username = ? COLLATE NOCASE
                  AND password = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("username"),
                            rs.getString("user_role"),
                            rs.getString("account_status")
                    );

                    return Optional.of(user);
                }

                return Optional.empty();
            }
        }
    }

    /**
     * Retrieves a user using their ID.
     */
    public Optional<User> findById(int userId)
            throws SQLException {

        String sql = """
                SELECT
                    user_id,
                    full_name,
                    username,
                    user_role,
                    account_status
                FROM "user"
                WHERE user_id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return Optional.of(
                            new User(
                                    rs.getInt("user_id"),
                                    rs.getString("full_name"),
                                    rs.getString("username"),
                                    rs.getString("user_role"),
                                    rs.getString("account_status")
                            )
                    );
                }

                return Optional.empty();
            }
        }
    }

    /**
     * Updates a user's account status.
     */
    public boolean updateAccountStatus(
            int userId,
            String accountStatus
    ) throws SQLException {

        if (!isValidAccountStatus(accountStatus)) {
            throw new IllegalArgumentException(
                    "Invalid account status. Allowed statuses: "
                            + "PENDING, ACTIVE, INACTIVE."
            );
        }

        String sql = """
                UPDATE "user"
                SET account_status = ?
                WHERE user_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, accountStatus);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Validates the three official system roles.
     */
    private boolean isValidRole(String role) {

        return role != null && (
                role.equals("ADMIN")
                        || role.equals("CASHIER")
                        || role.equals("EVENT COORDINATOR")
        );
    }

    /**
     * Validates account status values.
     */
    private boolean isValidAccountStatus(String status) {

        return status != null && (
                status.equals("PENDING")
                        || status.equals("ACTIVE")
                        || status.equals("INACTIVE")
        );
    }
}