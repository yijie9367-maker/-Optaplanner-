package com.wj.aischedule.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
public class SecurityAccessService {

    public boolean isTeacherOrAdmin(Long teacherId) {
        AuthenticatedUser user = currentUser();
        return user != null && (user.getRole() == AppRole.ADMIN
                || (user.getRole() == AppRole.TEACHER && user.getUserId().equals(teacherId)));
    }

    public boolean isStudentOrAdmin(Long studentId) {
        AuthenticatedUser user = currentUser();
        return user != null && (user.getRole() == AppRole.ADMIN
                || (user.getRole() == AppRole.STUDENT && user.getUserId().equals(studentId)));
    }

    public boolean isStudentClassOwnerOrAdmin(Long classGroupId) {
        AuthenticatedUser user = currentUser();
        return user != null && (user.getRole() == AppRole.ADMIN
                || (user.getRole() == AppRole.STUDENT && user.getClassGroupId() != null
                && user.getClassGroupId().equals(classGroupId)));
    }

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            return null;
        }
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
