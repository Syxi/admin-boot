package com.admin.module.system.service;

import java.util.List;
import java.util.Set;

/**
 * 角色缓存服务接口
 * 统一管理角色权限缓存和在线用户Token刷新
 * 
 * @author suYan
 */
public interface RoleCacheService {

    /**
     * 刷新所有角色的权限缓存
     */
    void refreshAllRolePermsCache();

    /**
     * 刷新指定角色的权限缓存
     * 
     * @param roleId 角色ID
     */
    void refreshRolePermsCache(Long roleId);

    /**
     * 刷新指定角色编码的权限缓存
     * 
     * @param roleCode 角色编码
     */
    void refreshRolePermsCacheByCode(String roleCode);

    /**
     * 批量刷新多个角色的权限缓存
     * 
     * @param roleIds 角色ID列表
     */
    void batchRefreshRolePermsCache(List<Long> roleIds);

    /**
     * 清除角色权限缓存
     * 
     * @param roleCode 角色编码
     */
    void clearRolePermsCache(String roleCode);

    /**
     * 获取角色权限（从缓存）
     * 
     * @param roleCode 角色编码
     * @return 权限集合
     */
    Set<String> getRolePermsFromCache(String roleCode);

    /**
     * 刷新拥有指定角色的所有在线用户Token
     * 当角色权限变更时，需要强制在线用户重新登录或刷新Token
     * 
     * @param roleCode 角色编码
     */
    void invalidateOnlineUsersByRole(String roleCode);

    /**
     * 刷新指定用户的缓存（用户角色变更时调用）
     * 
     * @param username 用户名
     */
    void invalidateUserCache(String username);
}
