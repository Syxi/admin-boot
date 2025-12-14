package com.admin.module.system.service.impl;

import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.StatusEnum;
import com.admin.module.system.entity.SysMenu;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.entity.SysUserRole;
import com.admin.module.system.mapper.SysMenuMapper;
import com.admin.module.system.mapper.SysRoleMapper;
import com.admin.module.system.mapper.SysUserRoleMapper;
import com.admin.module.system.service.RoleCacheDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色缓存数据访问服务实现类
 * 专门处理RoleCacheService所需的数据查询操作，避免循环依赖
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleCacheDataServiceImpl implements RoleCacheDataService {
    
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    
    /**
     * 获取所有未删除的角色列表
     */
    @Override
    public List<SysRole> getAllActiveRoles() {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getDeleted, DeletedEnum.NO_DELETE.getValue());
        return roleMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据角色ID获取角色
     */
    @Override
    public SysRole getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }
    
    /**
     * 根据角色编码获取角色
     */
    @Override
    public SysRole getRoleByCode(String roleCode) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getDeleted, DeletedEnum.NO_DELETE.getValue())
                .last("LIMIT 1");
        return roleMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据菜单ID列表获取菜单列表
     */
    @Override
    public List<SysMenu> getMenusByIds(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }
        
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysMenu::getMenuId, menuIds)
                .eq(SysMenu::getStatus, StatusEnum.ENABLE.getValue())
                .eq(SysMenu::getDeleted, DeletedEnum.NO_DELETE.getValue());
        return menuMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据角色ID获取用户角色关联列表
     */
    @Override
    public List<SysUserRole> getUserRolesByRoleId(Long roleId) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getRoleId, roleId);
        return userRoleMapper.selectList(queryWrapper);
    }
}