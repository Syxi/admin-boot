package com.admin.module.system.service;

import com.admin.module.system.entity.SysTenantConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 租户配置Service接口
 */
public interface SysTenantConfigService extends IService<SysTenantConfig> {
    /**
     * 获取租户配置信息
     * @param tenantId 租户ID
     * @return 租户配置信息
     */
    SysTenantConfig getTenantConfig(Long tenantId);

    /**
     * 更新租户用户数
     * @param tenantId 租户ID
     * @param increment 增量（正数为增加，负数为减少）
     */
    void updateTenantUserCount(Long tenantId, int increment);
}