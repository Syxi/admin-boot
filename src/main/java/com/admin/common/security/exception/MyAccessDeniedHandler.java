package com.admin.common.security.exception;


import com.admin.common.util.ResponseUtil;
import com.admin.common.result.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security 访问异常处理器
 * @Author: suYan
 * @Date: 2023-11-27
 */
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
       // 403
        ResponseUtil.writeErrorResponse(response, ResultCode.ACCESS_UNAUTHORIZED);
    }
}
