package cn.master.backend.handler.aspect;

import cn.master.backend.constants.Logical;
import cn.master.backend.entity.UserRolePermission;
import cn.master.backend.handler.annotation.HasAnyAuthorize;
import cn.master.backend.service.BaseUserRolePermissionService;
import cn.master.backend.util.Translator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author Created by 11's papa on 08/13/2024
 **/
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HasAnyAuthorizeAspect {
    private final BaseUserRolePermissionService baseUserRolePermissionService;

    /**
     * 定义切点 @Pointcut 在注解的位置切入代码
     */
    @Pointcut("@annotation(cn.master.backend.handler.annotation.HasAnyAuthorize)")
    public void hasAnyAuthorizePointCut() {
    }

    @Before("hasAnyAuthorizePointCut()")
    public void before(JoinPoint joinPoint) {
        log.info("[切面处理] >> 使用注解 @Before 调用了方法前置通知 ");
    }

    @After("hasAnyAuthorizePointCut()")
    public void afterMethod(JoinPoint joinPoint) {
        log.info("[切面处理] >> 使用注解 @After 调用了方法后置通知 ");
    }

    @AfterReturning(value = "hasAnyAuthorizePointCut()", returning = "result")
    public void validate(JoinPoint joinPoint, Object result) {
        log.info("[切面处理] >> 使用注解 @AfterReturning 调用了方法返回后通知 ");
    }

    @AfterThrowing(value = "hasAnyAuthorizePointCut()", throwing = "e")
    public void afterThrowingMethod(JoinPoint joinPoint, Exception e) {
        log.info("[切面处理] >> 使用注解 @AfterThrowing 调用了方法异常通知 ");
    }

    @Around("@annotation(anyAuthorize)")
    public Object aroundMethod(ProceedingJoinPoint pjp, HasAnyAuthorize anyAuthorize) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> rolePermissions = baseUserRolePermissionService.getPermissions(extractRoles(authentication))
                .stream().map(UserRolePermission::getPermissionId).toList();
        List<String> authorizes = Arrays.stream(anyAuthorize.value()).toList();
        // 鉴权逻辑
        boolean authorized = "administrator".equals(authentication.getName());
        if (anyAuthorize.logical().equals(Logical.AND)) {
            if (rolePermissions.stream().anyMatch(authorizes::contains)) {
                authorized = true;
            }
        } else {
            for (String s : authorizes) {
                if (rolePermissions.stream().anyMatch(s::equals)) {
                    authorized = true;
                    break;
                }
            }
        }
        if (authorized) {
            return pjp.proceed();
        } else {
            throw new AccessDeniedException(Translator.get("http_result_unauthorized"));
        }

    }

    private List<String> extractRoles(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }
}
