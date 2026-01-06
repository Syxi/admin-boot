package com.admin.module.system.service;

import java.util.Set;

/**
 * 租户套餐菜单权限服务接口
 */
public interface TenantPackageMenuService {
    
    /**
     * 获取套餐授权的菜单ID集合
     * @param packageId 套餐ID
     * @return 菜单ID集合
     */
    Set<Long> getPackageMenuIds(Long packageId);
    
    /**
     * 获取用户可见的菜单ID集合（套餐菜单 ∩ 角色菜单）
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return 菜单ID集合
     */
    Set<Long> getUserVisibleMenuIds(Long userId, Long tenantId);
}