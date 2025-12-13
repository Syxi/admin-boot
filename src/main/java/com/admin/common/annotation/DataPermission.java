package com.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限控制注解
 * 用于在Service方法上标记数据权限规则
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    
    /**
     * 表名（可选，用于更精确的权限控制）
     */
    String table() default "";
    
    /**
     * 部门字段名（默认为 dept_id）
     */
    String deptField() default "dept_id";
    
    /**
     * 用户字段名（默认为 create_user）
     */
    String userField() default "create_user";
    
    /**
     * 是否启用数据权限（默认启用）
     */
    boolean enable() default true;
}