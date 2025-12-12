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

}