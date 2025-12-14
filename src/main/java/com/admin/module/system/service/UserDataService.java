package com.admin.module.system.service;

import com.admin.module.system.entity.SysRole;

import java.util.List;

/**
 * 用户数据访问服务
 * 专门处理UserService所需的数据查询操作，避免循环依赖
 */
public interface UserDataService {
    
    /**
     * 根据角色ID列表获取角色列表
     */
    List<SysRole> getRolesByIds(List<Long> roleIds);
    
    /**
     * 根据角色ID列表获取角色列表（通过selectRoleList方法）
     */
    List<SysRole> selectRoleList(List<Long> roleIds);
}