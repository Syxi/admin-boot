package com.admin.module.system.service.impl;

import com.admin.module.system.entity.SysTenantUser;
import com.admin.module.system.mapper.SysTenantUserMapper;
import com.admin.module.system.service.SysTenantUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 70993
* @description 针对表【sys_tenant_user(租户用户关联表)】的数据库操作Service实现
* @createDate 2025-11-19 11:02:56
*/
@Service
public class SysTenantUserServiceImpl extends ServiceImpl<SysTenantUserMapper, SysTenantUser>
    implements SysTenantUserService{

    /**
     * 根据租户ids删除
     *
     * @param tenantIds
     */
    @Override
    public void removeByTenantIds(List<Long> tenantIds) {
        LambdaQueryWrapper<SysTenantUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysTenantUser::getTenantId, tenantIds);
        this.remove(queryWrapper);
    }

    /**
     * 根据租户id获取用户ids
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<Long> selectUserIdsByTenantId(Long tenantId) {
        LambdaQueryWrapper<SysTenantUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantUser::getTenantId, tenantId);
        List<Long> userIds = this.list(queryWrapper)
                .stream()
                .map(SysTenantUser::getUserId)
                .toList();
        return userIds;
    }
}




