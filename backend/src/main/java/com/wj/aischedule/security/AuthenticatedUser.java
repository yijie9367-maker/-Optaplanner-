package com.wj.aischedule.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class AuthenticatedUser implements Serializable {

    private final Long userId;
    private final String username;
    private final String displayName;
    private final AppRole role;
    private final Long classGroupId;

    public AuthenticatedUser(Long userId, String username, String displayName, AppRole role, Long classGroupId) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.classGroupId = classGroupId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AppRole getRole() {
        return role;
    }

    public String getRoleValue() {
        return role.getValue();
    }

    public Long getClassGroupId() {
        return classGroupId;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }
}
