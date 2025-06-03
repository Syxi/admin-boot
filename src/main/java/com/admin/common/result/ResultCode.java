package com.admin.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应枚举码
 * @Author: suYan
 * @Date: 2023-12-29
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResultCode implements Serializable {

    SUCCESS("200", "请求成功"),

    ERROR("500", "请求失败"),

    RESOURCE_NOT_FOUND("404", "资源不存在"),

    PASSWORD_ERROR("500", "密码不正确"),

    USERNAME_NOT_FOUND("500", "用户不存在"),

    TOKEN_INVALID("A320", "token已过期"),

    REFRESH_TOKEN_INVALID("A321", "刷新token已过期"),

    TOKEN_ACCESS_FORBIDDEN("500", "token已被禁止访问"),

    ACCESS_UNAUTHORIZED("500", "访问未授权"),

    CAPTCHA_CODE_TIMEOUT("500", "验证码过期"),

    CAPTCHA_ERROR("500", "验证码错误");





    private String code;

    private String msg;

    @Override
    public String toString() {
        return "{" +
                "\"code\":\"" + code + '\"' +
                ", \"msg\":\"" + msg + '\"' +
                '}';
    }


    public static ResultCode getValue(String code) {
        for (ResultCode value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ERROR;
    }
}
