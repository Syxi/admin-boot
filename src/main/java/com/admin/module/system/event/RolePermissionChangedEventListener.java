package com.admin.module.system.event;

import com.admin.module.system.service.RoleCacheService;
import com.admin.module.system.service.TokenRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 角色权限变更事件监听器
 * 负责处理角色权限变更后的缓存刷新和Token刷新
 * 
 * @author suYan
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RolePermissionChangedEventListener {

    private final RoleCacheService roleCacheService;
    private final TokenRefreshService tokenRefreshService;

    /**
     * 监听角色权限变更事件
     * 使用@Async异步处理，避免影响主流程性能
     */
    @Async
    @EventListener
    public void handleRolePermissionChanged(RolePermissionChangedEvent event) {
        log.info("接收到角色权限变更事件: changeType={}", event.getChangeType());

        try {
            switch (event.getChangeType()) {
                case ROLE_MENU_UPDATED:
                    // 角色菜单更新
                    handleRoleMenuUpdated(event);
                    break;

                case MENU_PERMISSION_UPDATED:
                case MENU_DELETED:
                    // 菜单权限变更或菜单删除，刷新所有角色
                    roleCacheService.refreshAllRolePermsCache();
                    log.info("菜单变更，已刷新所有角色权限缓存");
                    break;

                case ROLE_CODE_CHANGED:
                    // 角色编码变更
                    handleRoleCodeChanged(event);
                    break;

                case ROLE_DELETED:
                    // 角色删除
                    handleRoleDeleted(event);
                    break;

                case REFRESH_ALL:
                    // 全局刷新
                    roleCacheService.refreshAllRolePermsCache();
                    log.info("全局刷新，已刷新所有角色权限缓存");
                    break;

                default:
                    log.warn("未知的变更类型: {}", event.getChangeType());
            }
        } catch (Exception e) {
            log.error("处理角色权限变更事件失败: changeType={}", event.getChangeType(), e);
        }
    }

    /**
     * 处理角色菜单更新
     */
    private void handleRoleMenuUpdated(RolePermissionChangedEvent event) {
        if (event.getRoleId() != null && event.getRoleCode() != null) {
            // 刷新指定角色的权限缓存
            roleCacheService.refreshRolePermsCache(event.getRoleId());
            // 为拥有该角色的在线用户刷新Token
            tokenRefreshService.refreshOnlineUsersByRole(event.getRoleCode());
            log.info("角色菜单更新，已刷新缓存并为在线用户刷新Token: roleId={}, roleCode={}", 
                    event.getRoleId(), event.getRoleCode());
        }
    }

    /**
     * 处理角色编码变更
     */
    private void handleRoleCodeChanged(RolePermissionChangedEvent event) {
        if (event.getRoleCode() != null) {
            String oldRoleCode = event.getRoleCode();
            // 清除旧角色编码缓存
            roleCacheService.clearRolePermsCache(oldRoleCode);
            // 刷新新角色编码缓存（需要roleId）
            if (event.getRoleId() != null) {
                roleCacheService.refreshRolePermsCache(event.getRoleId());
            }
            // 为拥有旧角色的在线用户刷新Token
            tokenRefreshService.refreshOnlineUsersByRole(oldRoleCode);
            log.info("角色编码变更，已更新缓存并为在线用户刷新Token: oldRoleCode={}", oldRoleCode);
        }
    }

    /**
     * 处理角色删除
     */
    private void handleRoleDeleted(RolePermissionChangedEvent event) {
        if (event.getRoleCode() != null) {
            // 清除角色权限缓存
            roleCacheService.clearRolePermsCache(event.getRoleCode());
            // 为拥有该角色的在线用户刷新Token
            tokenRefreshService.refreshOnlineUsersByRole(event.getRoleCode());
            log.info("角色已删除，已清除缓存并为在线用户刷新Token: roleCode={}", event.getRoleCode());
        }
    }
}
