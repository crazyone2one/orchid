package cn.master.backend.util;

import cn.master.backend.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Created by 11's papa on 08/08/2024
 **/
@Slf4j
public class SessionUtils {
    private static final ThreadLocal<String> ORGANIZATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> PROJECT_ID = new ThreadLocal<>();

    public static CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static String getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * 权限验证时从 controller 参数列表中找到 organizationId 传入
     */
    public static void setCurrentOrganizationId(String organizationId) {
        SessionUtils.ORGANIZATION_ID.set(organizationId);
    }

    /**
     * 权限验证时从 controller 参数列表中找到 projectId 传入
     */
    public static void setCurrentProjectId(String projectId) {
        SessionUtils.PROJECT_ID.set(projectId);
    }

    public static String getCurrentOrganizationId() {
        if (StringUtils.isNotEmpty(ORGANIZATION_ID.get())) {
            return ORGANIZATION_ID.get();
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            if (request.getHeader("ORGANIZATION") != null) {
                return request.getHeader("ORGANIZATION");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return getCurrentUser().getOrganizationId();
    }

    public static String getCurrentProjectId() {
        if (StringUtils.isNotEmpty(PROJECT_ID.get())) {
            return PROJECT_ID.get();
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            if (request.getHeader("PROJECT") != null) {
                return request.getHeader("PROJECT");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return getCurrentUser().getProjectId();
    }
}
