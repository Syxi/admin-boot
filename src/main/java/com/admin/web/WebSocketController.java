package com.admin.web;

import com.admin.module.system.dto.MessageAckDTO;
import com.admin.module.system.service.WebSocketMessageConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketMessageConfirmationService webSocketMessageConfirmationService;

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
}
