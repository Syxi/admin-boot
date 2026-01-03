package com.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 租户存储空间限制检查注解
 * 用于标识需要检查租户存储空间限制的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantStorageLimitCheck {
    /**
     * 描述信息
     */
    String value() default "租户存储空间限制检查";
}