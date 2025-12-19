package com.admin.module.system.service;

import com.admin.module.system.dto.MessageAckDTO;
import com.admin.module.system.dto.WebSocketMessageDTO;

/**
 * WebSocket消息确认服务接口
 * 用于处理消息的确认和重试机制
 */
public interface WebSocketMessageConfirmationService {
    
    /**
     * 发送需要确认的消息
     * 
     * @param username 目标用户名
     * @param message 消息内容
     * @return 消息ID
     */
    String sendConfirmableMessage(String username, WebSocketMessageDTO message);
    
    /**
     * 处理客户端的消息确认
     * 
     * @param ack 确认信息
     */
    void handleMessageAcknowledgement(MessageAckDTO ack);
    
    /**
     * 重试未确认的消息
     * 
     * @param messageId 消息ID
     */
    void retryUnconfirmedMessage(String messageId);
    
    /**
     * 清理已确认的消息
     * 
     * @param messageId 消息ID
     */
    void cleanupConfirmedMessage(String messageId);
}