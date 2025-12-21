package com.admin.module.system.service.impl;

import com.admin.common.context.BaseServiceBeanContext;
import com.admin.common.security.SecurityConstants;
import com.admin.module.system.dto.WebSocketMessageDTO;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.service.RoleCacheDataService;
import com.admin.module.system.service.SysUserRoleService;
import com.admin.module.system.service.SysUserService;
import com.admin.module.system.service.WebSocketMessageConfirmationService;
import com.admin.module.system.service.WebSocketPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * WebSocket权限通知服务实现类
 * 用于向在线用户发送权限更新通知
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WebSocketPermissionServiceImpl implements WebSocketPermissionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebSocketMessageConfirmationService webSocketMessageConfirmationService;
    private final RoleCacheDataService roleCacheDataService;
    private final SysUserRoleService sysUserRoleService;
    
    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return this.redisTemplate;
    }
    
    /**
     * 向指定用户发送权限更新通知
     * 
     * @param username 用户名
     */
    @Override
    public void sendPermissionUpdateNotification(String username) {
        try {
            // 创建需要确认的消息
            WebSocketMessageDTO message = new WebSocketMessageDTO(
                UUID.randomUUID().toString(), 
                "PERMISSION_UPDATE", 
                "权限更新提醒", 
                "您的权限已更新，请刷新页面以获取最新权限"
            );
            
            // 发送需要确认的消息
            String messageId = webSocketMessageConfirmationService.sendConfirmableMessage(username, message);
            
            log.info("已向用户发送权限更新通知: username={}, messageId={}", username, messageId);
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
            // 1. 根据角色编码获取角色信息
            SysRole role = roleCacheDataService.getRoleByCode(roleCode);
            if (role == null) {
                log.debug("角色不存在: roleCode={}", roleCode);
                return;
            }
            
            // 2. 根据角色ID获取所有具有该角色的用户ID列表
            List<Long> userIdsWithRole = sysUserRoleService.selectUserIds(role.getRoleId());
            if (userIdsWithRole == null || userIdsWithRole.isEmpty()) {
                log.debug("没有用户拥有该角色: roleCode={}", roleCode);
                return;
            }
            
            // 3. 根据用户ID列表获取用户名列表
            List<String> usernamesWithRole = BaseServiceBeanContext.sysUserService.getUsernamesByIds(userIdsWithRole);
            if (usernamesWithRole == null || usernamesWithRole.isEmpty()) {
                log.debug("没有用户拥有该角色: roleCode={}", roleCode);
                return;
            }
            
            // 4. 获取所有在线用户
            Set<String> onlineUserKeys = redisTemplate.keys(SecurityConstants.ONLINE_USER_PREFIX + "*");
            if (onlineUserKeys == null || onlineUserKeys.isEmpty()) {
                log.debug("当前没有在线用户");
                return;
            }
            
            // 创建需要确认的消息
            WebSocketMessageDTO message = new WebSocketMessageDTO(
                UUID.randomUUID().toString(), 
                "PERMISSION_UPDATE", 
                "权限更新提醒", 
                "您的权限已更新，请刷新页面以获取最新权限"
            );
            
            int notifiedCount = 0;
            for (String key : onlineUserKeys) {
                try {
                    Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);
                    if (userData.isEmpty()) {
                        continue;
                    }
                    
                    String username = (String) userData.get("username");
                    // 5. 检查用户是否拥有该角色且在线
                    if (username != null && usernamesWithRole.contains(username)) {
                        // 发送需要确认的消息
                        String messageId = webSocketMessageConfirmationService.sendConfirmableMessage(username, message);
                        notifiedCount++;
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