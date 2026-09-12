package com.snp.admin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AdminAuthContext {

    private AdminAuthContext() {
    }

    public static Long currentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        Object p = auth.getPrincipal();
        if (p instanceof Long id) {
            return id;
        }
        return Long.parseLong(p.toString());
    }
}
