package com.admin.module.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;


    /**
     * 向用户发送信息
     * @param username
     * @param message
     */
    public void sendMessageToUser(String username, Object message) {
        messagingTemplate.convertAndSendToUser( username, "/queue/progress", message);
    }
}
