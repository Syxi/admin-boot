package com.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataPermission {

    String orgAlias() default "";

    String orgIdColumnName() default "id";

    String userAlias() default "";

    String userIdColumnName() default "created_by";
}
