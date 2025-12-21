package com.admin.module.system.service.impl;

import com.admin.module.system.dto.WebSocketMessageDTO;
import com.admin.module.system.service.WebSocketMessageConfirmationService;
import com.admin.module.system.service.WebSocketMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WebSocketMessageServiceImpl implements WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketMessageConfirmationService webSocketMessageConfirmationService;

    /**
     * 向用户发送信息
     * @param username 目标用户名
     * @param message 消息内容
     */
    @Override
    public void sendMessageToUser(String username, Object message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/progress", message);
    }

    /**
     * 向指定用户发送需要确认的消息
     * 
     * @param username 目标用户名
     * @param message 消息内容
     * @return 消息ID
     */
    @Override
    public String sendConfirmableMessageToUser(String username, WebSocketMessageDTO message) {
        return webSocketMessageConfirmationService.sendConfirmableMessage(username, message);
    }

    /**
     * 广播消息到所有订阅者
     * 
     * @param topic 广播主题
     * @param message 消息内容
     */
    @Override
    public void broadcastMessage(String topic, Object message) {
        messagingTemplate.convertAndSend(topic, message);
    }
}