package com.admin.module.system.service.impl;

import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.StatusEnum;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.mapper.SysRoleMapper;
import com.admin.module.system.service.UserDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户数据访问服务实现类
 * 专门处理UserService所需的数据查询操作，避免循环依赖
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataServiceImpl implements UserDataService {
    
    private final SysRoleMapper roleMapper;
    
    /**
     * 根据角色ID列表获取角色列表
     */
    @Override
    public List<SysRole> getRolesByIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return roleMapper.selectBatchIds(roleIds);
    }
    
    /**
     * 根据角色ID列表获取角色列表（通过selectRoleList方法）
     */
    @Override
    public List<SysRole> selectRoleList(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRole::getRoleId, roleIds);
        queryWrapper.eq(SysRole::getStatus, StatusEnum.ENABLE.getValue());
        queryWrapper.eq(SysRole::getDeleted, DeletedEnum.NO_DELETE.getValue());
        
        return roleMapper.selectList(queryWrapper);
    }
}