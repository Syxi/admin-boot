// com.admin.common.aspect.DataPermissionAspect.java
package com.admin.common.aspect;

import com.admin.common.annotation.DataPermission;
import com.admin.common.context.DataPermissionContext;
import com.admin.common.security.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 数据权限 AOP 切面
 * 在标记了 @DataPermission 的方法执行前，设置用户上下文
 */
@Aspect
@Component
public class DataPermissionAspect {

    @Around("@annotation(dataPermission)")
    public Object around(ProceedingJoinPoint joinPoint, DataPermission dataPermission) throws Throwable {
        try {
            // 从  SecurityContext 获取当前用户信息
            Long currentUserId = SecurityUtils.getUserId();      // 当前用户 ID
            Long currentDeptId = SecurityUtils.getDeptId();      // 所属部门 ID
            Integer dataScope = SecurityUtils.getDataScope();           // 权限范围：2 = 本部门及子部门

            DataPermissionContext.setUserId(currentUserId);
            DataPermissionContext.setDeptId(currentDeptId);
            DataPermissionContext.setDataScope(dataScope);

            return joinPoint.proceed();
        } finally {
            // 必须清除，避免线程复用导致数据污染
            DataPermissionContext.clear();
        }
    }
}