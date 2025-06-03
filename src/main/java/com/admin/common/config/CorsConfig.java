package com.admin.common.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

/**
 *  开启 cors资源共享
 * @author suYan
 * @date 2023/4/1 17:35
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cors = new CorsConfiguration();
        // 1.允许任何来源
        cors.setAllowedOriginPatterns(Collections.singletonList("*"));
        // 2.允许任何请求头
        cors.addAllowedHeader(CorsConfiguration.ALL);
        // 3.允许任何方法
        cors.addAllowedMethod(CorsConfiguration.ALL);
        // 4.允许凭证
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        CorsFilter corsFilter = new CorsFilter(source);
        FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>(corsFilter);
        filterRegistrationBean.setOrder(-101);
        return new CorsFilter(source);

    }
}
