package cn.master.backend.util;

import cn.master.backend.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
public class SessionUtils {
    public static CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static String getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
