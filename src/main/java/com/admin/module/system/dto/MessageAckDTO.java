package com.admin.module.system.dto;

import lombok.Data;

/**
 * 消息确认DTO
 * 用于客户端确认消息接收
 */
@Data
public class MessageAckDTO {
    
    /**
     * 消息ID
     */
    private String messageId;
    
    /**
     * 确认状态
     */
    private Boolean acknowledged;
    
    /**
     * 确认时间戳
     */
    private Long ackTimestamp;
    
    public MessageAckDTO() {
    }
    
    public MessageAckDTO(String messageId, Boolean acknowledged) {
        this.messageId = messageId;
        this.acknowledged = acknowledged;
        this.ackTimestamp = System.currentTimeMillis();
    }
}