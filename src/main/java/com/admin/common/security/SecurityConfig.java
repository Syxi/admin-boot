package com.admin.common.security;

import cn.hutool.captcha.generator.CodeGenerator;
import com.admin.common.properties.SecurityProperties;
import com.admin.common.security.exception.MyAccessDeniedHandler;
import com.admin.common.security.exception.MyAuthenticationEntryPoint;
import com.admin.common.security.filter.CaptchaValidationFilter;
import com.admin.common.security.filter.JwtValidationFilter;
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




    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable) // 关闭 csrf
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityConstants.LOGIN_PATH).permitAll() // 登录路径允许所有人访问
                        .anyRequest().authenticated()  // 其他所有请求都需要认证
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 设置会话创建策略为无状态
                )
                .exceptionHandling(ex -> ex
                                .authenticationEntryPoint(myAuthenticationEntryPoint) // 自定义身份认证入口点
                                .accessDeniedHandler(myAccessDeniedHandler) // 自定义访问拒绝处理器

                        );

        // 验证码校验过滤器
        httpSecurity.addFilterBefore(new CaptchaValidationFilter(redisTemplate, codeGenerator), UsernamePasswordAuthenticationFilter.class);

        // Jwt 校验过滤器
        httpSecurity.addFilterBefore(new JwtValidationFilter(tokenService), UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }


    /**
     * 不走过滤器链的放行配置
     * @return
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            if (CollectionUtils.isNotEmpty(securityProperties.getIgnoreUrls())) {
                web.ignoring().requestMatchers(securityProperties.getIgnoreUrls().toArray(new String[0]));
            }
        };
    }



    /**
     * 密码编码器
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 默认的密码认证 Provider
     * @return
     */
//    @Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider() {
//        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//        daoAuthenticationProvider.setUserDetailsService(sysUserDetailsService);
//        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
//        return daoAuthenticationProvider;
//    }


    /**
     *
     * 手动注入AuthenticationManager
     * - DaoAuthenticationProvider：用户密码认证
     * - XXX：其他微信认证
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }





}
