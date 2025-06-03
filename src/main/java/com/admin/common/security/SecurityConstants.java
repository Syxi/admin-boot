package com.admin.common.security;

/**
 * Security 常量
 * @author suYan
 * @date 2023/4/1 12:22
 */
public class SecurityConstants {

    /**
     * 登录接口路径
     */
    public static final String LOGIN_PATH = "/api/login";


    /**
     * 单账号登录，不能登录重复账号
     */
    public static final String REPEAT_LOGIN= "/api/repeatLogin";


    /**
     * Token 前缀
     */
    public static final String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * token的类型
     */
    public static final String JWT_TOKEN_TYPE = "Bearer";


    /**
     * 黑名单Token缓存前缀
     */
    public static final String BLACK_TOKEN_PREFIX = "Black_token";


    // 验证码缓存前缀
    public static final String CAPTCHA_CODE_PREFIX = "captcha_code";


}
