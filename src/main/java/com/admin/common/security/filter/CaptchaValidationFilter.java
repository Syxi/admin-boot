package com.admin.common.security.filter;

import cn.hutool.captcha.generator.CodeGenerator;
import com.admin.common.result.ResultCode;
import com.admin.common.security.SecurityConstants;
import com.admin.common.util.ResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 验证码校验过滤器
 * 在用户登录时校验验证码的正确性
 * 
 * @author suYan
 */
@Slf4j
@RequiredArgsConstructor
public class CaptchaValidationFilter extends OncePerRequestFilter {

    /** 登录路径匹配器 */
    private static final AntPathRequestMatcher LOGIN_MATCHER = 
            new AntPathRequestMatcher(SecurityConstants.LOGIN_PATH, "POST");

    /** 验证码Key参数名 */
    private static final String CAPTCHA_KEY_PARAM_NAME = "captchaKey";

    /** 验证码值参数名 */
    private static final String CAPTCHA_CODE_PARAM_NAME = "captchaCode";

    private final RedisTemplate<String, Object> redisTemplate;
    private final CodeGenerator codeGenerator;

    /**
     * 验证码校验过滤逻辑
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // 只校验登录接口
        if (!LOGIN_MATCHER.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取请求中的验证码
        String captchaCode = request.getParameter(CAPTCHA_CODE_PARAM_NAME);
        
        // 如果没有验证码，跳过校验（兼容没有验证码的情况）
        if (StringUtils.isBlank(captchaCode)) {
            log.debug("未提供验证码，跳过校验");
            filterChain.doFilter(request, response);
            return;
        }

        // 从 Redis 中获取缓存的验证码
        String captchaKey = request.getParameter(CAPTCHA_KEY_PARAM_NAME);
        if (StringUtils.isBlank(captchaKey)) {
            log.warn("验证码Key不能为空");
            ResponseUtil.writeErrorResponse(response, ResultCode.CAPTCHA_CODE_TIMEOUT);
            return;
        }

        String redisCaptchaKey = SecurityConstants.CAPTCHA_CODE_PREFIX + captchaKey;
        String cachedCaptchaCode = (String) redisTemplate.opsForValue().get(redisCaptchaKey);
        
        if (StringUtils.isBlank(cachedCaptchaCode)) {
            log.warn("验证码已过期或不存在: {}", captchaKey);
            ResponseUtil.writeErrorResponse(response, ResultCode.CAPTCHA_CODE_TIMEOUT);
            return;
        }

        // 校验验证码
        if (!codeGenerator.verify(cachedCaptchaCode, captchaCode)) {
            log.warn("验证码错误: 输入={}, 缓存={}", captchaCode, cachedCaptchaCode);
            ResponseUtil.writeErrorResponse(response, ResultCode.CAPTCHA_ERROR);
            return;
        }

        // 验证成功，删除缓存的验证码（一次性使用）
        redisTemplate.delete(redisCaptchaKey);
        log.debug("验证码校验成功");
        
        filterChain.doFilter(request, response);
    }
}
