package com.admin.module.system.service;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * WebSocket权限通知服务接口
 * 用于向在线用户发送权限更新通知
 */
public interface WebSocketPermissionService {
    
    /**
     * 向指定用户发送权限更新通知
     * 
     * @param username 用户名
     */
    void sendPermissionUpdateNotification(String username);
    
    /**
     * 向拥有指定角色的所有在线用户发送权限更新通知
     * 
     * @param roleCode 角色编码
     */
    void sendPermissionUpdateNotificationByRole(String roleCode);
    
    /**
     * 获取Redis模板
     * 
     * @return RedisTemplate
     */
    RedisTemplate<String, Object> getRedisTemplate();
}