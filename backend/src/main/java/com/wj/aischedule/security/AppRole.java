package com.wj.aischedule.security;

public enum AppRole {
    ADMIN("admin", "ROLE_ADMIN"),
    TEACHER("teacher", "ROLE_TEACHER"),
    STUDENT("student", "ROLE_STUDENT");

    private final String value;
    private final String authority;

    AppRole(String value, String authority) {
        this.value = value;
        this.authority = authority;
    }

    public String getValue() {
        return value;
    }

    public String getAuthority() {
        return authority;
    }

    public static AppRole fromValue(String value) {
        for (AppRole role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("不支持的角色: " + value);
    }
}
