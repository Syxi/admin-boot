package com.admin.common.aspect;

import com.admin.common.security.SecurityUtils;
import com.admin.common.security.TenantSecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 租户权限检查切面
 * 用于在方法执行前检查租户级别的权限和资源限制
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantPermissionAspect {

    private final TenantSecurityUtils tenantSecurityUtils;

    /**
     * 检查租户用户数限制的切点
     * 使用 @TenantUserLimitCheck 注解标识需要检查用户数限制的方法
     */
    @Before("@annotation(com.admin.common.annotation.TenantUserLimitCheck)")
    public void checkUserLimit(JoinPoint joinPoint) {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId != null && !tenantSecurityUtils.checkUserLimit(tenantId)) {
            log.warn("租户 {} 用户数超出限制", tenantId);
            throw new RuntimeException("租户用户数超出限制");
        }
    }

    /**
     * 检查租户存储空间限制的切点
     * 使用 @TenantStorageLimitCheck 注解标识需要检查存储空间限制的方法
     */
    @Before("@annotation(com.admin.common.annotation.TenantStorageLimitCheck)")
    public void checkStorageLimit(JoinPoint joinPoint) {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId != null && !tenantSecurityUtils.checkStorageLimit(tenantId)) {
            log.warn("租户 {} 存储空间超出限制", tenantId);
            throw new RuntimeException("租户存储空间超出限制");
        }
    }

    /**
     * 检查租户有效性的切点
     * 使用 @TenantValidityCheck 注解标识需要检查租户有效性的方法
     */
    @Before("@annotation(com.admin.common.annotation.TenantValidityCheck)")
    public void checkTenantValidity(JoinPoint joinPoint) {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId != null && !tenantSecurityUtils.checkTenantValidity(tenantId)) {
            log.warn("租户 {} 套餐已过期", tenantId);
            throw new RuntimeException("租户套餐已过期");
        }
    }
}