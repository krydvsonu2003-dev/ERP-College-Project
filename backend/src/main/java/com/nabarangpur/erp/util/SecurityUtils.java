package com.nabarangpur.erp.util;

import com.nabarangpur.erp.entity.User;
import com.nabarangpur.erp.exception.UnauthorizedException;
import com.nabarangpur.erp.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Convenience accessors for the currently authenticated principal.
 * Used by services to apply data-level filtering, e.g. a STUDENT can only
 * ever see rows that belong to their own student_id, and a FACULTY member
 * is scoped to their own faculty_subject_assignments.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static CustomUserDetails currentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new UnauthorizedException("No authenticated user in context");
        }
        return cud;
    }

    public static Long currentUserId() {
        return currentUserDetails().getId();
    }

    public static User currentDomainUser() {
        return currentUserDetails().getDomainUser();
    }

    public static boolean hasRole(String roleName) {
        return currentUserDetails().getRoleNames().contains(roleName);
    }

    public static boolean isStudent() {
        return hasRole("STUDENT");
    }

    public static boolean isFaculty() {
        return hasRole("FACULTY");
    }

    public static boolean isPrivileged() {
        return hasRole("SUPER_ADMIN") || hasRole("PRINCIPAL") || hasRole("HOD");
    }
}
