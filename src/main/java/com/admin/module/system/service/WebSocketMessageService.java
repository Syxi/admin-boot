package com.admin.module.system.service;

import com.admin.module.system.dto.WebSocketMessageDTO;

/**
 * WebSocket消息服务接口
 * 提供统一的WebSocket消息发送功能
 */
public interface WebSocketMessageService {

    /**
     * 向指定用户发送消息
     * 
     * @param username 目标用户名
     * @param message 消息内容
     */
    void sendMessageToUser(String username, Object message);

    /**
     * 向指定用户发送需要确认的消息
     * 
     * @param username 目标用户名
     * @param message 消息内容
     * @return 消息ID
     */
    String sendConfirmableMessageToUser(String username, WebSocketMessageDTO message);

    /**
     * 广播消息到所有订阅者
     * 
     * @param topic 广播主题
     * @param message 消息内容
     */
    void broadcastMessage(String topic, Object message);
}