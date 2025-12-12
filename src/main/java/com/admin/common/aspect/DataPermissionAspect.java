// com.admin.common.aspect.DataPermissionAspect.java
package com.admin.common.aspect;

import com.admin.common.context.DataPermissionContext;
import com.admin.common.enums.DataScopeEnum;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 数据权限 AOP 切面
 * 在标记了 @DataPermission 的方法执行前，设置用户上下文
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DataPermissionAspect {

    private final SysDeptService deptService;

    @Pointcut("@annotation(com.admin.common.annotation.DataPermission)")
    public void dataPermissionPointcut() {}

    @Around("dataPermissionPointcut()")
    public Object doDataPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Long userId = SecurityUtils.getUserId();
            Long deptId = SecurityUtils.getDeptId();
            Integer scope = SecurityUtils.getDataScope();
            if (scope == null || SecurityUtils.isAdmin() || scope.equals(DataScopeEnum.ALL.getValue())) {
                return joinPoint.proceed();
            }

            // 根据权限类型获取部门ID列表
            if ( scope.equals(DataScopeEnum.DEPT_AND_CHILDREN.getValue())) {
                // 包含当前部门以及子部门数据权限
                List<Long> deptIds = deptService.getAllSubDeptIds(deptId);
                // 设置权限上下文
                DataPermissionContext.setDeptIds(deptIds);
            } else if (scope.equals(DataScopeEnum.DEPT.getValue())) {
                // 仅当前部门数据权限
                List<Long> deptIds = Collections.singletonList(SecurityUtils.getDeptId());
                // 设置权限上下文
                DataPermissionContext.setDeptIds(deptIds);
            } else if (scope.equals(DataScopeEnum.CREATE_USER.getValue())) {
                // 仅本人数据权限
                DataPermissionContext.setUserId(userId);
            }


            // 执行业务方法
            return joinPoint.proceed();

        } finally {
            DataPermissionContext.clear();
        }
    }
}