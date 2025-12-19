package com.admin.module.system.service.impl;

import com.admin.module.system.dto.MessageAckDTO;
import com.admin.module.system.dto.WebSocketMessageDTO;
import com.admin.module.system.service.WebSocketMessageConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket消息确认服务实现类
 * 用于处理消息的确认和重试机制
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WebSocketMessageConfirmationServiceImpl implements WebSocketMessageConfirmationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 消息确认超时时间（秒）
    private static final long MESSAGE_ACK_TIMEOUT = 30;
    
    // 消息最大重试次数
    private static final int MAX_RETRY_COUNT = 3;
    
    // Redis中存储未确认消息的前缀
    private static final String UNCONFIRMED_MESSAGE_PREFIX = "websocket:unconfirmed:message:";
    
    // Redis中存储消息发送记录的前缀
    private static final String MESSAGE_SEND_RECORD_PREFIX = "websocket:message:send:record:";
    
    // Redis中存储消息目标用户的前缀
    private static final String MESSAGE_TARGET_USER_PREFIX = "websocket:message:target:user:";
    
    /**
     * 发送需要确认的消息
     * 
     * @param username 目标用户名
     * @param message 消息内容
     * @return 消息ID
     */
    @Override
    public String sendConfirmableMessage(String username, WebSocketMessageDTO message) {
        try {
            // 生成唯一消息ID
            if (message.getMessageId() == null) {
                message.setMessageId(UUID.randomUUID().toString());
            }
            
            // 发送消息
            messagingTemplate.convertAndSendToUser(username, "/queue/permission/update", message);
            
            // 记录消息发送
            String messageKey = UNCONFIRMED_MESSAGE_PREFIX + message.getMessageId();
            redisTemplate.opsForValue().set(messageKey, message, MESSAGE_ACK_TIMEOUT, TimeUnit.SECONDS);
            
            // 记录发送历史
            String recordKey = MESSAGE_SEND_RECORD_PREFIX + message.getMessageId();
            redisTemplate.opsForValue().set(recordKey, System.currentTimeMillis(), 24, TimeUnit.HOURS);
            
            // 记录消息目标用户
            String userKey = MESSAGE_TARGET_USER_PREFIX + message.getMessageId();
            redisTemplate.opsForValue().set(userKey, username, 24, TimeUnit.HOURS);
            
            log.info("已发送需要确认的消息: username={}, messageId={}, type={}", username, message.getMessageId(), message.getType());
            return message.getMessageId();
        } catch (Exception e) {
            log.error("发送需要确认的消息失败: username={}, message={}", username, message, e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 处理客户端的消息确认
     * 
     * @param ack 确认信息
     */
    @Override
    public void handleMessageAcknowledgement(MessageAckDTO ack) {
        try {
            if (ack.getAcknowledged() != null && ack.getAcknowledged()) {
                // 删除未确认消息记录
                String messageKey = UNCONFIRMED_MESSAGE_PREFIX + ack.getMessageId();
                redisTemplate.delete(messageKey);
                            
                // 删除目标用户记录
                String userKey = MESSAGE_TARGET_USER_PREFIX + ack.getMessageId();
                redisTemplate.delete(userKey);
                            
                // 删除发送记录
                String recordKey = MESSAGE_SEND_RECORD_PREFIX + ack.getMessageId();
                redisTemplate.delete(recordKey);
                
                log.info("消息确认成功: messageId={}", ack.getMessageId());
            } else {
                // 如果确认失败，增加重试次数或标记为发送失败
                String messageKey = UNCONFIRMED_MESSAGE_PREFIX + ack.getMessageId();
                WebSocketMessageDTO message = (WebSocketMessageDTO) redisTemplate.opsForValue().get(messageKey);
                if (message != null) {
                    message.setRetryCount(message.getRetryCount() + 1);
                    if (message.getRetryCount() < MAX_RETRY_COUNT) {
                        // 更新消息记录
                        redisTemplate.opsForValue().set(messageKey, message, MESSAGE_ACK_TIMEOUT, TimeUnit.SECONDS);
                        log.warn("消息确认失败，将进行重试: messageId={}, retryCount={}", ack.getMessageId(), message.getRetryCount());
                    } else {
                        // 达到最大重试次数，标记为发送失败
                        redisTemplate.delete(messageKey);
                        log.error("消息发送失败，达到最大重试次数: messageId={}", ack.getMessageId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理消息确认失败: ack={}", ack, e);
        }
    }
    
    /**
     * 重试未确认的消息
     * 
     * @param messageId 消息ID
     */
    @Override
    public void retryUnconfirmedMessage(String messageId) {
        try {
            String messageKey = UNCONFIRMED_MESSAGE_PREFIX + messageId;
            WebSocketMessageDTO message = (WebSocketMessageDTO) redisTemplate.opsForValue().get(messageKey);
            if (message != null) {
                // 增加重试次数
                message.setRetryCount(message.getRetryCount() + 1);
                
                if (message.getRetryCount() <= MAX_RETRY_COUNT) {
                    // 获取目标用户
                    String userKey = MESSAGE_TARGET_USER_PREFIX + messageId;
                    String targetUser = (String) redisTemplate.opsForValue().get(userKey);
                    
                    if (targetUser != null) {
                        // 重新发送消息
                        messagingTemplate.convertAndSendToUser(targetUser, "/queue/permission/update", message);
                        
                        // 更新消息记录
                        redisTemplate.opsForValue().set(messageKey, message, MESSAGE_ACK_TIMEOUT, TimeUnit.SECONDS);
                        log.info("重新发送消息: targetUser={}, messageId={}, retryCount={}", targetUser, messageId, message.getRetryCount());
                    } else {
                        log.warn("无法获取消息目标用户，无法重试: messageId={}", messageId);
                    }
                } else {
                    // 达到最大重试次数，标记为发送失败
                    redisTemplate.delete(messageKey);
                    log.error("消息重试失败，达到最大重试次数: messageId={}", messageId);
                }
            }
        } catch (Exception e) {
            log.error("重试未确认消息失败: messageId={}", messageId, e);
        }
    }
    
    /**
     * 清理已确认的消息
     * 
     * @param messageId 消息ID
     */
    @Override
    public void cleanupConfirmedMessage(String messageId) {
        try {
            // 删除未确认消息记录
            String messageKey = UNCONFIRMED_MESSAGE_PREFIX + messageId;
            redisTemplate.delete(messageKey);
            
            // 删除目标用户记录
            String userKey = MESSAGE_TARGET_USER_PREFIX + messageId;
            redisTemplate.delete(userKey);
            
            // 删除发送记录
            String recordKey = MESSAGE_SEND_RECORD_PREFIX + messageId;
            redisTemplate.delete(recordKey);
            
            log.info("清理已确认消息: messageId={}", messageId);
        } catch (Exception e) {
            log.error("清理已确认消息失败: messageId={}", messageId, e);
        }
    }
}