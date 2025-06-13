package com.admin.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.module.system.mapper.SysPositionDeptMapper;
import com.admin.module.system.entity.SysPositionDept;
import com.admin.module.system.service.SysPositionDeptService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_position_dept(岗位和组织关联表)】的数据库操作Service实现
* @createDate 2024-05-31 14:51:24
*/
@Service
public class SysPositionDeptServiceImpl extends ServiceImpl<SysPositionDeptMapper, SysPositionDept>
    implements SysPositionDeptService {

    /**
     * 新增岗位和组织关联关系
     *
     * @param positionId
     * @param deptId
     */
    @Override
    public void saveSysPositionDept(Long positionId, Long deptId) {
        SysPositionDept sysPositionDept = new SysPositionDept(positionId, deptId);
        this.save(sysPositionDept);
    }

    /**
     * 更新岗位和组织关联关系
     *
     * @param positionId
     * @param deptId
     */
    @Override
    public void updateSysPositionDept(Long positionId, Long deptId) {
        LambdaUpdateWrapper<SysPositionDept> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysPositionDept::getDeptId, deptId);
        updateWrapper.eq(SysPositionDept::getPositionId, positionId);
        this.update(updateWrapper);
    }

    /**
     * 删除岗位和组织关联关系
     *
     * @param positionIds
     */
    @Override
    public void deleteSysPositionDept(List<Long> positionIds) {
        LambdaQueryWrapper<SysPositionDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysPositionDept::getPositionId, positionIds);
        this.remove(queryWrapper);
    }

    /**
     * 删除岗位和组织关联关系
     *
     * @param deptIds
     */
    @Override
    public void deleteSysPositionDeptList(List<Long> deptIds) {
        LambdaQueryWrapper<SysPositionDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysPositionDept::getDeptId, deptIds);
        this.remove(queryWrapper);
    }

    /**
     * 获取组织id
     *
     * @param positionId
     * @return
     */
    @Override
    public Long getDeptId(Long positionId) {
        LambdaQueryWrapper<SysPositionDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPositionDept::getPositionId, positionId);
        queryWrapper.last("limit 1");
        SysPositionDept sysPositionDept = this.getOne(queryWrapper);
        return  sysPositionDept.getDeptId();
    }

}




