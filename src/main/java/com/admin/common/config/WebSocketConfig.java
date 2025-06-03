package com.admin.common.config;

import com.admin.common.security.SecurityConstants;
import com.admin.common.security.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * webSocket 配置
 *
 * 启用 webSocket 消息代理功能和配置STOMP协议，实现实时双向通信和消息传递
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenService tokenService;

    /**
     * 注册一个端点，客户端通过这个端点进行连接
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // 注册了一个 /ws 的端点
                .setAllowedOriginPatterns("*")  // 允许跨域的 WebSocket 连接
                .withSockJS(); // 启用 sock.js (浏览器不支持WebSocket，SockJS 将会提供兼容性支持)
    }


    /**
     * 配置消息代理
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端发送信息的请求前缀
        registry.setApplicationDestinationPrefixes("/app");

        // 客户端订阅消息的请求前缀，topic 一般用于广播推送，queue用于点对点推送
        registry.enableSimpleBroker("/topic");

        // 服务端通知客户端的前缀，可以不设置，默认为user
        registry.setUserDestinationPrefix("/user");
    }


    /**
     * 配置客户端入站通道拦截器
     * 添加 ChannelInterceptor 拦截器，用于消息发送前，从请求头中获取 token 并解析用户信息，用于点对点发送信息给指定用户
     * @param registration 通道注册器
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                // 如果是连接请求 (CONNECT 命令), 从请求头中取出 token 并设置到认证信息中
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 从请求头中提取授权令牌
                    String bearerToken = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

                    // 验证令牌格式并提取用户信息
                    if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
                        try {
                            bearerToken = bearerToken.substring(SecurityConstants.JWT_TOKEN_PREFIX.length());
                            String username = tokenService.getTokenClaims(bearerToken).getSubject();
                            if (StringUtils.isNotBlank(username)) {
                                accessor.setUser(() -> username);
                                return message;
                            }
                        } catch (Exception e) {
                            log.error("Failed to extract username from token", e);
                            throw new RuntimeException(e);
                        }
                    }
                }

                // 不是连接请求， 直接放行
                return ChannelInterceptor.super.preSend(message, channel);
            }
        });
    }




}
