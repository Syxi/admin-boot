package com.admin.common.security.filter;

import cn.hutool.captcha.generator.CodeGenerator;
import com.admin.common.util.ResponseUtil;
import com.admin.common.security.SecurityConstants;
import com.admin.common.result.ResultCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 验证码校验过滤器
 */
public class CaptchaValidationFilter extends OncePerRequestFilter {

    private static AntPathRequestMatcher LOGIN_MATCHER = new AntPathRequestMatcher(SecurityConstants.LOGIN_PATH, "POST");

    public static final String CAPTCHA_KEY_PARAM_NAME = "captchaKey";

    public static final String CAPTCHA_CODE_PARAM_NAME = "captchaCode";

    private final RedisTemplate<String, Object> redisTemplate;

    private final CodeGenerator codeGenerator;

    public CaptchaValidationFilter(RedisTemplate<String, Object> redisTemplate, CodeGenerator codeGenerator) {
        this.redisTemplate = redisTemplate;
        this.codeGenerator = codeGenerator;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 校验登录接口的验证码
        if (LOGIN_MATCHER.matches(request)) {
            // 请求中的验证码
            String captchaCode = request.getParameter(CAPTCHA_CODE_PARAM_NAME);

            // 兼容没有验证码的版本
            if (StringUtils.isBlank(captchaCode)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 缓存中的验证码
            String cacheCaptchaKey = request.getParameter(CAPTCHA_KEY_PARAM_NAME);
            String cacheCaptchaCode = (String) redisTemplate.opsForValue().get(SecurityConstants.CAPTCHA_CODE_PREFIX + cacheCaptchaKey);
            if (StringUtils.isBlank(cacheCaptchaCode)) {
                ResponseUtil.writeErrorResponse(response, ResultCode.CAPTCHA_CODE_TIMEOUT);
            } else {
                // 校验验证码
                if (codeGenerator.verify(cacheCaptchaCode, captchaCode)) {
                    filterChain.doFilter(request, response);
                } else {
                    ResponseUtil.writeErrorResponse(response, ResultCode.CAPTCHA_ERROR);
                }
            }
        } else {
            // 非登录接口方行
            filterChain.doFilter(request, response);
        }

    }
}
