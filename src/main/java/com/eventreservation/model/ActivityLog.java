package com.yourdomain.model;

public record ActivityLog(

        int logId,

        int userId,

        String actionType,

        String description,

        String timestamp

) {
}