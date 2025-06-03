package com.admin.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.SysPositionOrganization;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_position_organization(岗位和组织关联表)】的数据库操作Service
* @createDate 2024-05-31 14:51:24
*/
public interface SysPositionOrganizationService extends IService<SysPositionOrganization> {

    /**
     * 新增岗位和组织关联关系
     * @param positionId
     * @param organizationId
     */
    void saveSysPositionOrganization(Long positionId, Long organizationId);

    /**
     * 更新岗位和组织关联关系
     * @param positionId
     * @param organizationId
     */
    void  updateSysPositionOrganization(Long positionId, Long organizationId);


    /**
     * 删除岗位和组织关联关系
     * @param positionIds
     */
    void deleteSysPositionOrganization(List<Long> positionIds);

    /**
     * 删除岗位和组织关联关系
     * @param organIds
     */
    void deleteSysPositionOrgan(List<Long> organIds);


    /**
     * 获取组织id
     * @param positionId
     * @return
     */
    Long getOrganId(Long positionId);
}
