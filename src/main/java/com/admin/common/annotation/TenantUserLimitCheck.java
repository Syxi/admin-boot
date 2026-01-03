package com.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 租户用户数限制检查注解
 * 用于标识需要检查租户用户数限制的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantUserLimitCheck {
    /**
     * 描述信息
     */
    String value() default "租户用户数限制检查";
}