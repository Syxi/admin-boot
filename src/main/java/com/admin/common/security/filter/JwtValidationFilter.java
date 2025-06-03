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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Jwt token 校验过滤器
 * @author suYan
 * @date 2023/9/23 22:24
 */
@Slf4j
public class JwtValidationFilter extends OncePerRequestFilter {


    private final TokenService tokenService;



    public JwtValidationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }




    /**
     * 从请求中获取 JWT token， 校验 JWT token 的合法性
     * 如果合法，则将 Authentication 设置到 spring Security Context 上下文中
     * 如果不合法，则清空 Spring Security Context 上下文，并直接返回响应
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            if (StringUtils.isNotBlank(token) && token.startsWith(SecurityConstants.JWT_TOKEN_PREFIX)) {
                // 去除 Bearer 前缀
                token = token.substring(SecurityConstants.JWT_TOKEN_PREFIX.length());



                // 验证 token是否有效
                if (!tokenService.validateToken(token)) {
                    // token 无效，直接返回
                    log.error("token 无效: {}",  token);
                    ResponseUtil.writeErrorResponse(response, ResultCode.TOKEN_INVALID);
                    return;
                }


                // token 有效将其解析为 Authentication 对象，并设置到 Spring Security 上下文
                Authentication authentication = tokenService.parseToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);


            }
        } catch (CustomException e) {
            SecurityContextHolder.clearContext();
            ResponseUtil.writeErrorResponse(response, ResultCode.TOKEN_INVALID);
            return;
        }

        // Token有效或无Token时继续执行过滤链
        filterChain.doFilter(request, response);
    }







}
