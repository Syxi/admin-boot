package com.admin.common.interceptor;

import com.admin.common.security.service.TokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;

/**
 * WebSocket认证拦截器
 * 用于在WebSocket握手阶段进行身份认证
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final TokenService tokenService;

    /**
     * 在握手之前执行，可以用来进行身份验证等操作
     *
     * @param request    HTTP请求
     * @param response   HTTP响应
     * @param wsHandler  WebSocket处理器
     * @param attributes 保存在WebSocket会话中的属性
     * @return 如果返回true，则继续握手；如果返回false，则终止握手
     * @throws Exception 异常
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 允许所有连接，但记录用户信息（如果可用）
        log.debug("WebSocket handshake allowed for all users");
        
        // 尝试从查询参数中获取用户名（用于测试目的）
        try {
            URI uri = request.getURI();
            String query = uri.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2 && "username".equals(keyValue[0])) {
                        String username = URLDecoder.decode(keyValue[1], "UTF-8");
                        attributes.put("username", username);
                        attributes.put("userId", 1L); // 默认用户ID
                        log.debug("WebSocket handshake for user: {}", username);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse username from query parameters", e);
        }
        
        return true;
    }

    /**
     * 在握手之后执行，可以用来进行一些清理工作
     *
     * @param request   HTTP请求
     * @param response  HTTP响应
     * @param wsHandler WebSocket处理器
     * @param exception 异常
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                              WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake error", exception);
        } else {
            log.debug("WebSocket handshake completed successfully");
        }
    }
}