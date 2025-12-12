package com.admin.common.handler;

import com.admin.common.context.DataPermissionContext;
import com.admin.common.enums.DataScopeEnum;
import com.admin.common.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 权限工具类（用于 Service 快速应用)
 *
 * @author YourName
 * @since 2025-12-11
 */
@Component
public class DataPermissionHelper {

    /**
     * 一键应用所有数据权限（自动处理部门/用户权限）
     * @param wrapper 查询条件
     * @param deptIdFunc 部门ID字段函数（可为null）
     * @param userIdFunc 创建者ID字段函数（可为null）
     * @param frontendDeptId 前端传的部门ID（可为null）
     */
    public static <T> void applyPermissions(
            LambdaQueryWrapper<T> wrapper,
            SFunction<T, Long> deptIdFunc,
            SFunction<T, Long> userIdFunc,
            Long frontendDeptId) {

        Integer scope = SecurityUtils.getDataScope();
        // 1. 自动应用部门权限（如果配置了部门字段）
        if (!scope.equals(DataScopeEnum.CREATE_USER.getValue())) {
            applyDeptPermission(wrapper, deptIdFunc, frontendDeptId);
        } else {
            applyUserPermission(wrapper, userIdFunc);
        }
    }

    /**
     * 应用部门权限（自动融合前端传参）
     * @param wrapper 查询条件
     * @param deptIdFunc 部门ID字段函数
     * @param frontendDeptId 前端传的部门ID（可为null）
     */
    public static <T> void applyDeptPermission(
            LambdaQueryWrapper<T> wrapper,
            SFunction<T, Long> deptIdFunc,
            Long frontendDeptId) {

        List<Long> permittedDeptIds = DataPermissionContext.getDeptIds();
        if (CollectionUtils.isEmpty(permittedDeptIds)) {
            // 无权限（由Service层处理空返回）
            return;
        }

        // 前端传了部门ID → 检查是否在权限范围内
        if (frontendDeptId != null) {
            if (!permittedDeptIds.contains(frontendDeptId)) {
                // 无权访问该部门 → 重置条件为无结果
                wrapper.eq(deptIdFunc, -1L); // 生成无效条件
            } else {
                // 有效 → 保留前端条件
                wrapper.eq(deptIdFunc, frontendDeptId);
            }
        } else {
            // 前端未传 → 应用权限范围
            wrapper.in(deptIdFunc, permittedDeptIds);
        }
    }

    /**
     * 应用创建者权限
     * @param wrapper 查询条件
     * @param userIdFunc 创建者ID字段函数
     */
    public static <T> void applyUserPermission(
            LambdaQueryWrapper<T> wrapper,
            SFunction<T, Long> userIdFunc) {

        Long currentUserId = DataPermissionContext.getUserId();
        if (currentUserId == null) {
            return;
        }
        wrapper.eq(userIdFunc, currentUserId);
    }
}