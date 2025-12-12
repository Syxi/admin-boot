// com.admin.common.context.DataPermissionContext.java
package com.admin.common.context;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据权限上下文，用于在线程内传递 @DataPermission 注解信息
 * 配合 AOP 切面使用，在进入 Service 方法时存入，MyBatis 拦截器读取
 */
@Component
public class DataPermissionContext {


    private static final ThreadLocal<List<Long>> DEPT_IDS = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setDeptIds(List<Long> deptIds) {
        DEPT_IDS.set(deptIds);
    }

    public static List<Long> getDeptIds() {
        return DEPT_IDS.get();
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        DEPT_IDS.remove();
        USER_ID.remove();
    }

}