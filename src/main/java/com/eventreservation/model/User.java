package com.eventreservation.model;

public record User(
        int userId,
        String fullName,
        String username,
        String userRole,
        String accountStatus
) {
}