package com.admin.module.system.service;

import com.admin.module.system.entity.SysTenantUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 70993
* @description 针对表【sys_tenant_user(租户用户关联表)】的数据库操作Service
* @createDate 2025-11-19 11:02:56
*/
public interface SysTenantUserService extends IService<SysTenantUser> {

    /**
     * 根据租户ids删除
     *
     * @param tenantIds
     */
    void removeByTenantIds(List<Long> tenantIds);

    /**
     * 根据租户id获取用户ids
     *
     * @param tenantId
     * @return
     */
    List<Long> selectUserIdsByTenantId(Long tenantId);

}
