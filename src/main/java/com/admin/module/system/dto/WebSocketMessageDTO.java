package com.admin.module.system.dto;

import lombok.Data;

/**
 * WebSocket消息传输对象
 * 用于封装需要确认的消息内容
 */
@Data
public class WebSocketMessageDTO {
    
    /**
     * 消息ID，用于唯一标识一条消息
     */
    private String messageId;
    
    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 消息内容
     */
    private Object payload;
    
    /**
     * 发送时间戳
     */
    private Long timestamp;
    
    /**
     * 重试次数
     */
    private Integer retryCount = 0;
    
    public WebSocketMessageDTO() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public WebSocketMessageDTO(String messageId, String type, Object payload) {
        this();
        this.messageId = messageId;
        this.type = type;
        this.payload = payload;
    }
}