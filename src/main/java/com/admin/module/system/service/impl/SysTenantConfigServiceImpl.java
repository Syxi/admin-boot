package com.admin.module.system.service.impl;

import com.admin.common.enums.StatusEnum;
import com.admin.module.system.entity.SysTenantConfig;
import com.admin.module.system.mapper.SysTenantConfigMapper;
import com.admin.module.system.service.SysTenantConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 租户配置Service实现类
 */
@Service
@RequiredArgsConstructor
public class SysTenantConfigServiceImpl extends ServiceImpl<SysTenantConfigMapper, SysTenantConfig> implements SysTenantConfigService {

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
}