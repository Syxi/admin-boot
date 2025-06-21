package com.admin.module.system.service.impl;

import com.admin.module.system.entity.SysUserDept;
import com.admin.module.system.mapper.SysUserDeptMapper;
import com.admin.module.system.service.SysUserDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_user_dept(用户和部门关联表)】的数据库操作Service实现
* @createDate 2025-06-20 16:28:53
*/
@Service
public class SysUserDeptServiceImpl extends ServiceImpl<SysUserDeptMapper, SysUserDept>
    implements SysUserDeptService{

    /**
     * 根据用户ID, 更新部门ID
     *
     * @param userId
     * @param deptId
     */
    @Override
    public void updateByUserId(Long userId, Long deptId) {
        LambdaUpdateWrapper<SysUserDept> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUserDept::getUserId, userId);
        updateWrapper.set(SysUserDept::getDeptId, deptId);

        boolean result = this.update(updateWrapper);
        if (!result) {
            this.save(new SysUserDept(userId, deptId));
        }
    }

    /**
     * 根据用户ID, 删除
     * @param userIds
     */
    @Override
    public void deleteBatchByUserId(List<Long> userIds) {
        LambdaQueryWrapper<SysUserDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUserDept::getUserId, userIds);
        this.remove(queryWrapper);
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public Long selectDeptId(Long userId) {
        LambdaQueryWrapper<SysUserDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserDept::getUserId, userId);
        queryWrapper.last("limit 1");
        SysUserDept sysUserDept = this.getOne(queryWrapper);

        if (sysUserDept == null) {
            return null;
        }
        return  sysUserDept.getDeptId();
    }


}




