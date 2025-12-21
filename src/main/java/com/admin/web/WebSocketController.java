package com.admin.web;

import com.admin.module.system.dto.MessageAckDTO;
import com.admin.module.system.dto.WebSocketMessageDTO;
import com.admin.module.system.service.WebSocketMessageConfirmationService;
import com.admin.module.system.service.WebSocketMessageService;
import com.admin.module.system.service.WebSocketPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketMessageConfirmationService webSocketMessageConfirmationService;
    private final WebSocketMessageService webSocketMessageService;
    private final WebSocketPermissionService webSocketPermissionService;

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public String handleMessage(String message) {
        System.out.println("收到前端传来的消息：" + message);
        //广播消息到 前端订阅的地址
        return "后端发送消息到前端：服务器已接收到你的消息！";
    }

    /**
     * 处理客户端发送的消息确认
     * 
     * @param ack 确认信息
     */
    @MessageMapping("/acknowledge")
    public void handleAcknowledgement(@Payload MessageAckDTO ack) {
        System.out.println("收到前端传来的消息确认：" + ack);
        try {
            // 处理消息确认
            webSocketMessageConfirmationService.handleMessageAcknowledgement(ack);
        } catch (Exception e) {
            System.err.println("处理消息确认失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送测试消息给指定用户
     * 
     * @param message 消息内容
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/sendToUser")
    public void sendToUser(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 获取当前用户信息
            Principal principal = headerAccessor.getUser();
            if (principal != null) {
                String username = principal.getName();
                
                // 创建测试消息
                Map<String, Object> payload = new HashMap<>();
                payload.put("content", message);
                payload.put("timestamp", System.currentTimeMillis());
                payload.put("type", "TEST_MESSAGE");
                
                WebSocketMessageDTO webSocketMessage = new WebSocketMessageDTO(
                    UUID.randomUUID().toString(),
                    "TEST_MESSAGE",
                    "测试消息",
                    payload
                );
                
                // 发送需要确认的消息
                webSocketMessageService.sendConfirmableMessageToUser(username, webSocketMessage);
                
                System.out.println("已向用户发送测试消息: username=" + username + ", message=" + message);
            }
        } catch (Exception e) {
            System.err.println("发送测试消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 广播测试消息
     * 
     * @param message 消息内容
     */
    @MessageMapping("/broadcast")
    @SendTo("/topic/broadcast")
    public Map<String, Object> broadcastMessage(@Payload String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", message);
        response.put("timestamp", System.currentTimeMillis());
        response.put("type", "BROADCAST");
        
        System.out.println("已广播测试消息: " + message);
        return response;
    }

    /**
     * 发送权限更新通知给指定用户
     * 用于测试权限更新通知功能
     * 
     * @param username 目标用户名
     */
    @MessageMapping("/sendPermissionUpdate")
    public void sendPermissionUpdate(@Payload String username) {
        try {
            webSocketPermissionService.sendPermissionUpdateNotification(username);
        } catch (Exception e) {
            System.err.println("发送权限更新通知失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
