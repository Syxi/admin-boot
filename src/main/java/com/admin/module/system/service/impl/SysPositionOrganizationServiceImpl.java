package com.admin.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.module.system.mapper.SysPositionOrganizationMapper;
import com.admin.module.system.entity.SysPositionOrganization;
import com.admin.module.system.service.SysPositionOrganizationService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_position_organization(岗位和组织关联表)】的数据库操作Service实现
* @createDate 2024-05-31 14:51:24
*/
@Service
public class SysPositionOrganizationServiceImpl extends ServiceImpl<SysPositionOrganizationMapper, SysPositionOrganization>
    implements SysPositionOrganizationService{

    /**
     * 新增岗位和组织关联关系
     *
     * @param positionId
     * @param organizationId
     */
    @Override
    public void saveSysPositionOrganization(Long positionId, Long organizationId) {
        SysPositionOrganization sysPositionOrganization = new SysPositionOrganization(positionId, organizationId);
        this.save(sysPositionOrganization);
    }

    /**
     * 更新岗位和组织关联关系
     *
     * @param positionId
     * @param organizationId
     */
    @Override
    public void updateSysPositionOrganization(Long positionId, Long organizationId) {
        LambdaUpdateWrapper<SysPositionOrganization> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysPositionOrganization::getOrganId, organizationId);
        updateWrapper.eq(SysPositionOrganization::getPositionId, positionId);
        this.update(updateWrapper);
    }

    /**
     * 删除岗位和组织关联关系
     *
     * @param positionIds
     */
    @Override
    public void deleteSysPositionOrganization(List<Long> positionIds) {
        LambdaQueryWrapper<SysPositionOrganization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysPositionOrganization::getPositionId, positionIds);
        this.remove(queryWrapper);
    }

    /**
     * 删除岗位和组织关联关系
     *
     * @param organIds
     */
    @Override
    public void deleteSysPositionOrgan(List<Long> organIds) {
        LambdaQueryWrapper<SysPositionOrganization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysPositionOrganization::getOrganId, organIds);
        this.remove(queryWrapper);
    }

    /**
     * 获取组织id
     *
     * @param positionId
     * @return
     */
    @Override
    public Long getOrganId(Long positionId) {
        LambdaQueryWrapper<SysPositionOrganization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPositionOrganization::getPositionId, positionId);
        queryWrapper.last("limit 1");
        SysPositionOrganization sysPositionOrganization = this.getOne(queryWrapper);
        return  sysPositionOrganization.getOrganId();
    }

}




