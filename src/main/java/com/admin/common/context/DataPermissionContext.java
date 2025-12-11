// com.admin.common.context.DataPermissionContext.java
package com.admin.common.context;

import com.admin.common.security.SecurityUtils;

/**
 * 数据权限上下文，用于在线程内传递 @DataPermission 注解信息
 * 配合 AOP 切面使用，在进入 Service 方法时存入，MyBatis 拦截器读取
 */
public class DataPermissionContext {


    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_DEPT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> CURRENT_DATA_SCOPE = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setDeptId(Long deptId) {
        CURRENT_DEPT_ID.set(deptId);
    }

    public static Long getDeptId() {
        return CURRENT_DEPT_ID.get();
    }

    public static void setDataScope(Integer dataScope) {
        CURRENT_DATA_SCOPE.set(dataScope);
    }

    public static Integer getDataScope() {
        return CURRENT_DATA_SCOPE.get();
    }

    /**
     * 判断是否为超级管理员（）
     */
    public static boolean isAdmin() {
        return getUserId() != null && SecurityUtils.isAdmin();
    }

    /**
     * 清除当前线程的上下文（防止内存泄漏）
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_DEPT_ID.remove();
        CURRENT_DATA_SCOPE.remove();
    }
}