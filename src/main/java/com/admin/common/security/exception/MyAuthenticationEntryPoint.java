package com.admin.common.security.exception;

import com.admin.common.result.ResultCode;
import com.admin.common.util.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

;

/**
 * 登录认证异常处理
 * @Author: suYan
 * @Date: 2023-11-27
 */
@Slf4j
@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        int status = response.getStatus();
        if (status == HttpServletResponse.SC_NOT_FOUND) {
            // 404 资源不存在
            log.error(ResultCode.RESOURCE_NOT_FOUND.getMsg());
            ResponseUtil.writeErrorResponse(response, ResultCode.RESOURCE_NOT_FOUND);
        } else {
            if (authException instanceof BadCredentialsException) {
                // 权限校验失败，密码错误
                log.error(authException.getMessage());
                ResponseUtil.writeErrorResponse(response, ResultCode.PASSWORD_ERROR);
            } else if (authException instanceof InternalAuthenticationServiceException) {
                // 权限校验失败，用户名不存在
                log.error(authException.getMessage());
                ResponseUtil.writeErrorResponse(response, ResultCode.USERNAME_NOT_FOUND);
            }  else {
                // 未认证或者token过期
                log.error(authException.getMessage());
                ResponseUtil.writeErrorResponse(response, ResultCode.TOKEN_INVALID);
            }
        }
    }
}
