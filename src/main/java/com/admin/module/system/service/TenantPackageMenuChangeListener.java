package com.admin.module.system.service;

import com.admin.module.system.entity.SysTenantConfig;
import com.admin.module.system.mapper.SysTenantConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户套餐菜单变更监听服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantPackageMenuChangeListener {

    private final RoleCacheService roleCacheService;
    private final SysTenantConfigMapper tenantConfigMapper;

    /**
     * 当套餐的菜单权限发生变更时，更新所有使用该套餐的租户的权限缓存
     * @param packageId 套餐ID
     */
    public void onPackageMenuChanged(Long packageId) {
        try {
            // 查询所有使用该套餐的租户
            LambdaQueryWrapper<SysTenantConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysTenantConfig::getPackageId, packageId)
                    .eq(SysTenantConfig::getStatus, 1); // 只查询启用状态的配置

            List<SysTenantConfig> tenantConfigs = tenantConfigMapper.selectList(queryWrapper);

            // 对每个使用该套餐的租户，清除其用户的权限缓存
            for (SysTenantConfig config : tenantConfigs) {
                Long tenantId = config.getTenantId();
                try {
                    roleCacheService.invalidateTenantUserCache(tenantId);
                    log.info("套餐菜单变更，已更新租户用户权限缓存: packageId={}, tenantId={}", packageId, tenantId);
                } catch (Exception e) {
                    log.error("更新租户用户权限缓存失败: packageId={}, tenantId={}", packageId, tenantId, e);
                }
            }
        } catch (Exception e) {
            log.error("处理套餐菜单变更事件失败: packageId={}", packageId, e);
        }
    }
}