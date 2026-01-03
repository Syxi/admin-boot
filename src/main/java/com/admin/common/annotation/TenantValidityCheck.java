package com.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 租户有效性检查注解
 * 用于标识需要检查租户有效性的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantValidityCheck {
    /**
     * 描述信息
     */
    String value() default "租户有效性检查";
}