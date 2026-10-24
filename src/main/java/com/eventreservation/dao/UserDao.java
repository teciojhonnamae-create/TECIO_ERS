package com.yourdomain.dao;

import com.yourdomain.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

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

    /**
     * Returns all registered accounts, regardless of role or status.
     */
    public List<User> getAllUsers() throws SQLException {

        String sql = """
                SELECT user_id, full_name, username,
                       user_role, account_status
                FROM "user"
                ORDER BY user_id
                """;

        return executeUserListQuery(sql);
    }

    /**
     * Returns all accounts that are awaiting approval.
     */
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

    /**
     * Approves an account only if its current status is PENDING.
     *
     * @return true if exactly one account was approved
     */
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