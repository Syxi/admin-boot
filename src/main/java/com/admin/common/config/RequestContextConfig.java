package com.admin.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

/**
 * 请求上下文配置
 * 用于在异步处理中传递请求上下文信息
 */
@Configuration
public class RequestContextConfig {

    /**
     * 配置请求上下文监听器
     * 支持在异步线程中获取HTTP请求信息
     * @return RequestContextListener
     */
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}