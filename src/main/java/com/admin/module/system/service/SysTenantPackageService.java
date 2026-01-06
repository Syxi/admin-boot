package com.admin.module.system.service;

import com.admin.module.system.entity.SysTenantPackage;
import com.admin.module.system.query.TenantPackageQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 租户套餐Service接口
 */
public interface SysTenantPackageService extends IService<SysTenantPackage> {

    /**
     * 获取租户套餐分页列表
     * @param tenantPackageQuery
     * @return
     */
    IPage<SysTenantPackage> selectTenantPackagePage(TenantPackageQuery tenantPackageQuery);
}