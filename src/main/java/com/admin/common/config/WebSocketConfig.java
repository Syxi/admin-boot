package com.admin.common.config;

import com.admin.common.interceptor.WebSocketAuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * webSocket 配置
 *
 * 启用 webSocket 消息代理功能和配置STOMP协议，实现实时双向通信和消息传递
 */
@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    /**
     * 注册一个端点，客户端通过这个端点进行连接
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // 注册了一个 /ws 的端点
                .setAllowedOriginPatterns("*")  // 允许跨域的 WebSocket 连接
                // 添加认证拦截器
                .withSockJS()
                .setHeartbeatTime(10000)  // 设置心跳时间为10秒
                .setDisconnectDelay(30000); // 设置断开连接延迟为30秒
                
        // 注册一个备用端点，不使用SockJS
        registry.addEndpoint("/ws-alt")
                .setAllowedOriginPatterns("*");
//                .addInterceptors(webSocketAuthInterceptor) // 添加认证拦截器
//                .setHandshakeHandler(new DefaultHandshakeHandler() {
//                    @Override
//                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
//                        // 可以在这里添加用户认证逻辑
//                        return super.determineUser(request, wsHandler, attributes);
//                    }
//                });
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
        // 启用心跳检测，提高连接稳定性
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000})  // 设置心跳间隔为10秒
                .setTaskScheduler(customMessageBrokerTaskScheduler());

        // 服务端通知客户端的前缀，可以不设置，默认为user
        registry.setUserDestinationPrefix("/user");
    }
    
    /**
     * 配置消息代理任务调度器
     * @return
     */
    @Bean
    public TaskScheduler customMessageBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("WebSocketMessageBroker-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }



}
