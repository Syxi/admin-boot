package com.admin.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 会话方式
     */
    private SessionProperty session;

    /**
     * jwt 配置
     */
    private JwtProperty jwt;


    /**
     * 白名单 url 集合
     */
    private List<String> ignoreUrls ;


    /**
     * 会话属性
     */
    @Data
    public static class SessionProperty {

        /**
         * 会话方式
         */
        private String type;
    }


    /**
     * jwt 配置
     */
    @Data
    public static class JwtProperty {

        /**
         * jwt 密钥
         */
        private String key;

        /**
         * 访问令牌有效期（单位：秒)
         */
        private Long accessTokenTimeTOLive;

        /**
         * 刷新令牌有效期（单位：秒)
         */
        private Long refreshTokenTimeTOLive;
    }
}
