
package com.yourdomain.dao;

import com.yourdomain.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

public class UserDao {

    // Check whether a username already exists.
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

    // Register a new user.
    public void register(
            String fullName,
            String username,
            String password,
            String userRole
    ) throws SQLException {

        String sql = """
                INSERT INTO "user" (
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
            ps.setString(4, userRole);

            ps.executeUpdate();
        }
    }

    // Login user.
    public Optional<User> login(
            String username,
            String password
    ) throws SQLException {

        String sql = """
                SELECT user_id, full_name, username,
                       user_role, account_status
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
                    return Optional.of(mapUser(rs));
                }

                return Optional.empty();
            }
        }
    }

    // Retrieve all registered users.
    public List<User> getAllUsers() throws SQLException {

        String sql = """
                SELECT user_id, full_name, username,
                       user_role, account_status
                FROM "user"
                ORDER BY user_id
                """;

        return executeUserListQuery(sql);
    }

    // Retrieve all pending users.
    public List<User> getPendingUsers() throws SQLException {

        String sql = """
                SELECT user_id, full_name, username,
                       user_role, account_status
                FROM "user"
                WHERE account_status = 'PENDING'
                ORDER BY user_id
                """;

        return executeUserListQuery(sql);
    }

    // Approve a pending user account.
    public boolean approveUser(int userId)
            throws SQLException {

        String sql = """
                UPDATE "user"
                SET account_status = 'APPROVED'
                WHERE user_id = ?
                  AND account_status = 'PENDING'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() == 1;
        }
    }

    // NEW METHOD:
    // Update the selected user's role and account status.
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus
    ) throws SQLException {

        if (newRole == null || newStatus == null) {
            throw new IllegalArgumentException(
                    "Role and status cannot be null."
            );
        }

        newRole = newRole.trim().toUpperCase(Locale.ROOT);
        newStatus = newStatus.trim().toUpperCase(Locale.ROOT);

        String sql = """
                UPDATE "user"
                SET user_role = ?, account_status = ?
                WHERE user_id = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, newRole);
            ps.setString(2, newStatus);
            ps.setInt(3, userId);

            return ps.executeUpdate() == 1;
        }
    }

    // Execute queries that return a list of users.
    private List<User> executeUserListQuery(String sql)
            throws SQLException {

        List<User> users = new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }

        return users;
    }

    // Convert database records into User objects.
    private User mapUser(ResultSet rs)
            throws SQLException {

        return new User(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("username"),
                rs.getString("user_role"),
                rs.getString("account_status")
        );
    }
}
