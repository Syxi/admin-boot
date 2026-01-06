package com.admin.module.system.service.impl;

import com.admin.common.enums.StatusEnum;
import com.admin.module.system.entity.SysTenantConfig;
import com.admin.module.system.entity.SysTenantPackage;
import com.admin.module.system.mapper.SysTenantConfigMapper;
import com.admin.module.system.mapper.SysTenantPackageMapper;
import com.admin.module.system.service.SysTenantConfigService;
import com.admin.module.system.service.RoleCacheService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 租户配置Service实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysTenantConfigServiceImpl extends ServiceImpl<SysTenantConfigMapper, SysTenantConfig> implements SysTenantConfigService {

    private final SysTenantPackageMapper tenantPackageMapper;
    private final RoleCacheService roleCacheService;

    @Override
    public SysTenantConfig getTenantConfig(Long tenantId) {
        LambdaQueryWrapper<SysTenantConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantConfig::getTenantId, tenantId)
                .eq(SysTenantConfig::getStatus, StatusEnum.ENABLE.getValue()) // 只查询启用状态的配置
                .orderByDesc(SysTenantConfig::getCreateTime) // 按创建时间倒序，获取最新的配置
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public void updateTenantUserCount(Long tenantId, int increment) {
        SysTenantConfig config = getTenantConfig(tenantId);
        if (config != null) {
            // 更新当前用户数
            Integer newCount = (config.getCurrentUsers() == null ? 0 : config.getCurrentUsers()) + increment;
            config.setCurrentUsers(newCount);
            config.setUpdateTime(LocalDateTime.now());
            this.updateById(config);
        }
    }

    @Override
    public boolean assignPackageToTenant(Long tenantId, Long packageId) {
        // 验证租户和套餐是否存在
        // 这里可以添加验证逻辑，确保tenantId和packageId是有效的
        
        // 创建新的租户配置记录
        SysTenantConfig config = new SysTenantConfig();
        config.setTenantId(tenantId);
        config.setPackageId(packageId);
        config.setStatus(StatusEnum.ENABLE.getValue()); // 设置为启用状态
        config.setStartTime(LocalDateTime.now());
        
        // 获取套餐的有效期天数并计算结束时间
        SysTenantPackage tenantPackage = tenantPackageMapper.selectById(packageId);
        if (tenantPackage != null && tenantPackage.getValidityDays() != null) {
            config.setEndTime(LocalDateTime.now().plusDays(tenantPackage.getValidityDays()));
        }
        
        // 先删除该租户的现有配置（更新状态为禁用）
        LambdaQueryWrapper<SysTenantConfig> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysTenantConfig::getTenantId, tenantId);
        // 更新现有配置为禁用状态
        LambdaUpdateWrapper<SysTenantConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysTenantConfig::getTenantId, tenantId)
                   .set(SysTenantConfig::getStatus, -2); // 禁用状态
        this.update(updateWrapper);
        
        // 保存新的配置
        boolean result = this.save(config);
        
        // 如果套餐分配成功，更新该租户所有用户的权限缓存
        if (result) {
            try {
                roleCacheService.invalidateTenantUserCache(tenantId);
            } catch (Exception e) {
                // 记录日志但不中断操作
                log.warn("更新租户用户权限缓存失败: tenantId={}", tenantId, e);
            }
        }
        
        return result;
    }

    @Override
    public SysTenantConfig getTenantCurrentConfig(Long tenantId) {
        LambdaQueryWrapper<SysTenantConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantConfig::getTenantId, tenantId)
                .orderByDesc(SysTenantConfig::getCreateTime) // 按创建时间倒序，获取最新的配置
                .last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}