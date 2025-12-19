package com.admin.module.system.service.impl;

import com.admin.common.security.SecurityConstants;
import com.admin.module.system.dto.WebSocketMessageDTO;
import com.admin.module.system.service.WebSocketMessageConfirmationService;
import com.admin.module.system.service.WebSocketPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * WebSocket权限通知服务实现类
 * 用于向在线用户发送权限更新通知
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WebSocketPermissionServiceImpl implements WebSocketPermissionService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebSocketMessageConfirmationService webSocketMessageConfirmationService;
    
    /**
     * 向指定用户发送权限更新通知
     * 
     * @param username 用户名
     */
    @Override
    public void sendPermissionUpdateNotification(String username) {
        try {
            // 创建需要确认的消息
            WebSocketMessageDTO message = new WebSocketMessageDTO(UUID.randomUUID().toString(), "PERMISSION_UPDATE", "PERMISSION_UPDATED");
            
            // 发送需要确认的消息
            webSocketMessageConfirmationService.sendConfirmableMessage(username, message);
            
            log.info("已向用户发送权限更新通知: username={}", username);
        } catch (Exception e) {
            log.error("向用户发送权限更新通知失败: username={}", username, e);
        }
    }
    
    /**
     * 向拥有指定角色的所有在线用户发送权限更新通知
     * 
     * @param roleCode 角色编码
     */
    @Override
    public void sendPermissionUpdateNotificationByRole(String roleCode) {
        try {
            // 获取所有在线用户
            Set<String> onlineUserKeys = redisTemplate.keys(SecurityConstants.ONLINE_USER_PREFIX + "*");
            if (onlineUserKeys == null || onlineUserKeys.isEmpty()) {
                log.debug("当前没有在线用户");
                return;
            }
            
            int notifiedCount = 0;
            for (String key : onlineUserKeys) {
                try {
                    Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);
                    if (userData.isEmpty()) {
                        continue;
                    }
                    
                    String username = (String) userData.get("username");
                    // 发送权限更新通知给用户
                    if (username != null) {
                        // 创建需要确认的消息
                        WebSocketMessageDTO message = new WebSocketMessageDTO(UUID.randomUUID().toString(), "PERMISSION_UPDATE", "PERMISSION_UPDATED");
                        
                        // 发送需要确认的消息
                        webSocketMessageConfirmationService.sendConfirmableMessage(username, message);
                        
                        notifiedCount++;
                        log.debug("已向用户发送权限更新通知: username={}, roleCode={}", username, roleCode);
                    }
                } catch (Exception e) {
                    log.error("向用户发送权限更新通知失败: key={}", key, e);
                }
            }
            
            log.info("角色权限变更，已向 {} 个在线用户发送权限更新通知: roleCode={}", notifiedCount, roleCode);
        } catch (Exception e) {
            log.error("向在线用户发送权限更新通知失败: roleCode={}", roleCode, e);
        }
    }
}