package com.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限控制注解
 * 标记在 Service 方法上，表示该方法需要进行数据权限过滤
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
}