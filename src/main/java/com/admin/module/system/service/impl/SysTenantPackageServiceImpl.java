package com.admin.module.system.service.impl;

import com.admin.module.system.entity.SysTenantPackage;
import com.admin.module.system.query.TenantPackageQuery;
import com.admin.module.system.mapper.SysTenantPackageMapper;
import com.admin.module.system.service.SysTenantPackageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 租户套餐Service实现类
 */
@Service
@RequiredArgsConstructor
public class SysTenantPackageServiceImpl extends ServiceImpl<SysTenantPackageMapper, SysTenantPackage> implements SysTenantPackageService {

    @Override
    public IPage<SysTenantPackage> selectTenantPackagePage(TenantPackageQuery tenantPackageQuery) {
        LambdaQueryWrapper<SysTenantPackage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(tenantPackageQuery.getName()), SysTenantPackage::getName, tenantPackageQuery.getName())
                .like(StringUtils.isNotBlank(tenantPackageQuery.getCode()), SysTenantPackage::getCode, tenantPackageQuery.getCode())
                .eq(tenantPackageQuery.getStatus() != null, SysTenantPackage::getStatus, tenantPackageQuery.getStatus())
                .orderByAsc(SysTenantPackage::getSort);
        
        Page<SysTenantPackage> page = new Page<>(tenantPackageQuery.getPage(), tenantPackageQuery.getLimit());
        return this.page(page, queryWrapper);
    }
}