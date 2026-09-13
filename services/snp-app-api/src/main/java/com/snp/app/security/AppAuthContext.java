package com.snp.app.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AppAuthContext {

    private AppAuthContext() {
    }

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || !auth.isAuthenticated()) {
            return null;
        }
        Object p = auth.getPrincipal();
        if ("anonymousUser".equals(p)) {
            return null;
        }
        if (p instanceof Long id) {
            return id;
        }
        try {
            return Long.parseLong(p.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public static Long requireUserId() {
        Long id = currentUserId();
        if (id == null) {
            throw new com.snp.common.exception.BizException(
                    com.snp.common.api.ErrorCode.UNAUTHORIZED);
        }
        return id;
    }
}
