package com.admin.common.security.filter;

import com.admin.common.exception.CustomException;
import com.admin.common.result.ResultCode;
import com.admin.common.security.SecurityConstants;
import com.admin.common.security.service.TokenService;
import com.admin.common.util.ResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Token校验过滤器
 * 负责从请求头中提取JWT Token，校验其有效性，并将认证信息设置到Security上下文中
 * 
 * @author suYan
 * @date 2023/9/23 22:24
 */
@Slf4j
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    /**
     * JWT过滤器核心逻辑
     * 1. 从请求头中提取Token
     * 2. 校验Token有效性
     * 3. 将认证信息设置到Security上下文
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
        
        String token = extractToken(request);
        
        // 如果没有Token，直接跳过
        if (StringUtils.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 校验Token有效性
            if (!tokenService.validateToken(token)) {
                log.warn("Token校验失败，请求路URL: {}", request.getRequestURI());
                handleInvalidToken(response);
                return;
            }

            // 解析Token并设置认证信息
            Authentication authentication = tokenService.parseToken(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("Token校验成功，用户: {}", authentication.getName());
            
        } catch (CustomException e) {
            log.error("Token处理异常: {}", e.getMessage());
            handleInvalidToken(response);
            return;
        } catch (Exception e) {
            log.error("Token校验未知异常", e);
            handleInvalidToken(response);
            return;
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT Token
     *
     * @param request HTTP请求
     * @return Token字符串，如果不存在则返回null
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (StringUtils.isBlank(authHeader)) {
            return null;
        }
        
        if (!authHeader.startsWith(SecurityConstants.JWT_TOKEN_PREFIX)) {
            log.debug("Authorization header格式不正确");
            return null;
        }
        
        return authHeader.substring(SecurityConstants.JWT_TOKEN_PREFIX.length()).trim();
    }

    /**
     * 处理无效Token，清空Security上下文并返回错误响应
     *
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        ResponseUtil.writeErrorResponse(response, ResultCode.TOKEN_INVALID);
    }
}
