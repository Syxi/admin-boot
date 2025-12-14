package com.admin.module.system.service;

import com.admin.module.system.entity.SysMenu;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.entity.SysUserRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;

/**
 * 角色缓存数据访问服务
 * 专门处理RoleCacheService所需的数据查询操作，避免循环依赖
 */
public interface RoleCacheDataService {
    
    /**
     * 获取所有未删除的角色列表
     */
    List<SysRole> getAllActiveRoles();
    
    /**
     * 根据角色ID获取角色
     */
    SysRole getRoleById(Long roleId);
    
    /**
     * 根据角色编码获取角色
     */
    SysRole getRoleByCode(String roleCode);
    
    /**
     * 根据菜单ID列表获取菜单列表
     */
    List<SysMenu> getMenusByIds(List<Long> menuIds);
    
    /**
     * 根据角色ID获取用户角色关联列表
     */
    List<SysUserRole> getUserRolesByRoleId(Long roleId);
}