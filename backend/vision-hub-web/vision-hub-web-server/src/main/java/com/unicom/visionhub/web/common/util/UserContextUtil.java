package com.unicom.visionhub.web.common.util;

import com.unicom.visionhub.web.entity.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public final class UserContextUtil {

    private UserContextUtil() {
    }


    public static String getUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }


    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }


    public static Long getUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
}