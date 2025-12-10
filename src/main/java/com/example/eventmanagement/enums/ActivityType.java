package com.example.eventmanagement.enums;

public enum ActivityType {

    // User activities
    USER_CREATE("USER_CREATE", "User Created"),
    USER_UPDATE("USER_UPDATE", "User Updated"),
    USER_DELETE("USER_DELETE", "User Deleted"),
    USER_ACTIVATE("USER_ACTIVATE", "User Activated"),
    USER_DEACTIVATE("USER_DEACTIVATE", "User Deactivated"),

    // Event activities
    EVENT_CREATE("EVENT_CREATE", "Event Created"),
    EVENT_UPDATE("EVENT_UPDATE", "Event Updated"),
    EVENT_DELETE("EVENT_DELETE", "Event Deleted"),

    // Role activities
    ROLE_CREATE("ROLE_CREATE", "Role Created"),
    ROLE_UPDATE("ROLE_UPDATE", "Role Updated"),
    ROLE_DELETE("ROLE_DELETE", "Role Deleted"),
    ROLE_ASSIGN_PERMISSION("ROLE_ASSIGN_PERMISSION", "Role Permission Assigned"),
    ROLE_REMOVE_PERMISSION("ROLE_REMOVE_PERMISSION", "Role Permission Removed"),

    // Permission activities
    PERMISSION_CREATE("PERMISSION_CREATE", "Permission Created"),
    PERMISSION_UPDATE("PERMISSION_UPDATE", "Permission Updated"),
    PERMISSION_DELETE("PERMISSION_DELETE", "Permission Deleted"),

    // Password activities
    PASSWORD_CHANGE("PASSWORD_CHANGE", "Password Changed"),
    PASSWORD_RESET("PASSWORD_RESET", "Password Reset");

    private final String code;
    private final String description;

    ActivityType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
