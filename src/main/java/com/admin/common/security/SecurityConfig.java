package com.admin.common.security;

import cn.hutool.captcha.generator.CodeGenerator;
import com.admin.common.properties.SecurityProperties;
import com.admin.common.security.exception.MyAccessDeniedHandler;
import com.admin.common.security.exception.MyAuthenticationEntryPoint;
import com.admin.common.security.filter.CaptchaValidationFilter;
import com.admin.common.security.filter.JwtValidationFilter;
import com.admin.common.security.filter.RedisTokenValidationFilter;
import com.admin.common.security.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 * 
 * @author suYan
 * @date 2023/4/6 22:44
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CodeGenerator codeGenerator;
    private final MyAuthenticationEntryPoint myAuthenticationEntryPoint;
    private final MyAccessDeniedHandler myAccessDeniedHandler;
    private final SecurityProperties securityProperties;
    private final TokenService tokenService;

    /**
     * 配置Security过滤器链
     * 
     * @param httpSecurity HttpSecurity实例
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 关闭CSRF防护，因为使用JWT不需要CSRF保护
                .csrf(AbstractHttpConfigurer::disable)
                // 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstants.LOGIN_PATH).permitAll()
                        .anyRequest().authenticated()
                )
                // 设置会话管理为无状态，不创建Session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 配置异常处理
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(myAuthenticationEntryPoint)
                        .accessDeniedHandler(myAccessDeniedHandler)
                );

        // 添加验证码校验过滤器
        httpSecurity.addFilterBefore(
                new CaptchaValidationFilter(redisTemplate, codeGenerator),
                UsernamePasswordAuthenticationFilter.class
        );

        // 根据配置添加相应的Token校验过滤器
        String sessionType = securityProperties.getSession().getType();
        if ("jwt".equals(sessionType)) {
            // 添加JWT校验过滤器
            httpSecurity.addFilterBefore(
                new JwtValidationFilter(tokenService),
                UsernamePasswordAuthenticationFilter.class
            );
        } else if ("redis-token".equals(sessionType)) {
            // 添加Redis Token校验过滤器
            httpSecurity.addFilterBefore(
                new RedisTokenValidationFilter(tokenService),
                UsernamePasswordAuthenticationFilter.class
            );
        }

        return httpSecurity.build();
    }

    /**
     * 配置不走过滤器链的放行路径
     * 
     * @return WebSecurityCustomizer
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            if (CollectionUtils.isNotEmpty(securityProperties.getIgnoreUrls())) {
                web.ignoring().requestMatchers(
                        securityProperties.getIgnoreUrls().toArray(new String[0])
                );
            }
        };
    }

    /**
     * 密码加密器，使用BCrypt加密算法
     * 
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器，用于处理认证请求
     * 
     * @param authenticationConfiguration 认证配置
     * @return AuthenticationManager
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
