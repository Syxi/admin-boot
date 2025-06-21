package com.admin.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.SysPositionDept;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_position_dept(岗位和组织关联表)】的数据库操作Service
* @createDate 2024-05-31 14:51:24
*/
public interface SysPositionDeptService extends IService<SysPositionDept> {

    /**
     * 新增岗位和组织关联关系
     * @param positionId
     * @param deptId
     */
    void saveSysPositionDept(Long positionId, Long deptId);

    /**
     * 更新岗位和组织关联关系
     * @param positionId
     * @param deptId
     */
    void updateSysPositionDept(Long positionId, Long deptId);


    /**
     * 删除岗位和组织关联关系
     * @param positionIds
     */
    void deleteSysPositionDept(List<Long> positionIds);

    /**
     * 删除岗位和组织关联关系
     * @param deptIds
     */
    void deleteSysPositionDeptList(List<Long> deptIds);


    /**
     * 获取组织id
     * @param positionId
     * @return
     */
    Long getDeptId(Long positionId);
}
