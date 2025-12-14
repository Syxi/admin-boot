package com.admin.module.system.service;

import com.admin.module.system.entity.SysRole;

import java.util.List;
import java.util.Set;

/**
 * 菜单数据访问服务
 * 专门处理MenuService所需的数据查询操作，避免循环依赖
 */
public interface MenuDataService {
    
    /**
     * 根据角色编码获取角色ID集合
     */
    Set<Long> getRoleIdsByRoleCodes(Set<String> roleCodes);
    
    /**
     * 根据角色ID获取角色
     */
    SysRole getRoleById(Long roleId);
}