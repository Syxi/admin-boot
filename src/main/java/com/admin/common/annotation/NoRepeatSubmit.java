package com.admin.common.annotation;

import java.lang.annotation.*;

/**
 * 防止重复提交
 * @Author: suYan
 * @Date: 2023-12-07
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface NoRepeatSubmit {

    /**
     * 防重复提交锁过期时间(秒)
     *
     * 默认5秒内不允许重复提交
     */
    int expire() default 5;
}
