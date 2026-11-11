package com.yourdomain.dao;

import com.yourdomain.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // ==========================================
    // 1. INSERT A NEW ACTIVITY LOG
    // ==========================================

    public void logAction(
            int userId,
            String actionType,
            String description
    ) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user ID."
            );
        }

        if (actionType == null || actionType.isBlank()) {
            throw new IllegalArgumentException(
                    "Action type cannot be empty."
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be empty."
            );
        }

        String sql = """
                INSERT INTO activity_logs (
                    user_id,
                    action_type,
                    description
                )
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);

            ps.executeUpdate();
        }
    }


    // ==========================================
    // 2. RETRIEVE ALL ACTIVITY LOGS
    // ==========================================

    public List<ActivityLog> getAllLogs()
            throws SQLException {

        String sql = """
                SELECT
                    log_id,
                    user_id,
                    action_type,
                    description,
                    timestamp
                FROM activity_logs
                ORDER BY timestamp DESC, log_id DESC
                """;

        return executeLogQuery(sql, null);
    }


    // ==========================================
    // 3. RETRIEVE LOGS BY USER ID
    // ==========================================

    public List<ActivityLog> getLogsByUserId(
            int userId
    ) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero."
            );
        }

        String sql = """
                SELECT
                    log_id,
                    user_id,
                    action_type,
                    description,
                    timestamp
                FROM activity_logs
                WHERE user_id = ?
                ORDER BY timestamp DESC, log_id DESC
                """;

        return executeLogQuery(sql, userId);
    }


    // ==========================================
    // 4. CLEAR HISTORICAL LOGS SAFELY
    // ==========================================

    public int clearAllLogs(
            int adminUserId
    ) throws SQLException {

        if (adminUserId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid administrator ID."
            );
        }

        String deleteSql = """
                DELETE FROM activity_logs
                """;

        String insertAuditSql = """
                INSERT INTO activity_logs (
                    user_id,
                    action_type,
                    description
                )
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            boolean originalAutoCommit =
                    connection.getAutoCommit();

            try {

                connection.setAutoCommit(false);

                int deletedCount;

                // Delete historical records.
                try (PreparedStatement deleteStatement =
                             connection.prepareStatement(deleteSql)) {

                    deletedCount =
                            deleteStatement.executeUpdate();
                }

                // Record the administrator's clearing action.
                String description =
                        "Admin [" + adminUserId
                                + "] cleared "
                                + deletedCount
                                + " historical audit log(s).";

                try (PreparedStatement insertStatement =
                             connection.prepareStatement(
                                     insertAuditSql
                             )) {

                    insertStatement.setInt(1, adminUserId);

                    insertStatement.setString(
                            2,
                            "LOGS_CLEARED"
                    );

                    insertStatement.setString(
                            3,
                            description
                    );

                    insertStatement.executeUpdate();
                }

                connection.commit();

                return deletedCount;

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


    // ==========================================
    // 5. REUSABLE QUERY EXECUTION
    // ==========================================

    private List<ActivityLog> executeLogQuery(
            String sql,
            Integer userId
    ) throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection connection =
                     DatabaseConnection.getConnection();

             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            if (userId != null) {
                ps.setInt(1, userId);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    ActivityLog log = new ActivityLog(
                            rs.getInt("log_id"),
                            rs.getInt("user_id"),
                            rs.getString("action_type"),
                            rs.getString("description"),
                            rs.getString("timestamp")
                    );

                    logs.add(log);
                }
            }
        }

        return logs;
    }
}