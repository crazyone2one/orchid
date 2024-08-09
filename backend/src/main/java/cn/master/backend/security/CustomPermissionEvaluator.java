package cn.master.backend.security;

import cn.master.backend.entity.UserRolePermission;
import cn.master.backend.service.BaseUserRolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Created by 11's papa on 08/07/2024
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {
    private final BaseUserRolePermissionService baseUserRolePermissionService;

    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object permission) {
        if ((Objects.isNull(authentication)) || Objects.isNull(target) || !(permission instanceof String)) {
            return false;
        }
        if (!(target instanceof String)) {
            return false;
        }
        return hasPrivilege(authentication, (String) target, permission.toString().toUpperCase());
    }


    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (Objects.isNull(authentication) || (Objects.isNull(targetType)) || (targetId == null) || !(permission instanceof String)) {
            return false;
        }
        return hasPrivilege(authentication, targetType.toUpperCase(), permission.toString().toUpperCase());
    }

    private boolean hasPrivilege(Authentication authentication, String target, String permission) {
        if ("administrator".equals(authentication.getName())) {
            return true;
        }
        List<UserRolePermission> rolePermissions = baseUserRolePermissionService.getPermissions(extractRoles(authentication));
        if (rolePermissions.isEmpty()) {
            return false;
        }
        return rolePermissions.stream().anyMatch(rolePermission -> rolePermission.getPermissionId().equals(target + ":" + permission));
    }

    private List<String> extractRoles(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    public boolean isAdmin() {
        // TODO: Implement
        return true;
    }

    public boolean hasAuthority(String authority) {
        return hasAnyAuthority(authority);
    }

    public boolean hasAnyAuthority(String... authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<UserRolePermission> rolePermissions = baseUserRolePermissionService.getPermissions(extractRoles(authentication));
        if (rolePermissions.isEmpty()) {
            return false;
        }
        List<String> list = rolePermissions.stream().map(UserRolePermission::getPermissionId).toList();
        val tmpAuthorities = Arrays.asList(authority[0].split(","));
        return tmpAuthorities.stream().anyMatch(list::contains);
    }
}
