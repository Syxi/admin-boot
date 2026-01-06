package com.admin.module.system.service.impl;

import com.admin.common.security.SecurityUtils;
import com.admin.module.system.entity.SysMenu;
import com.admin.module.system.entity.SysTenantConfig;
import com.admin.module.system.entity.SysTenantPackageMenu;
import com.admin.module.system.entity.SysUserRole;
import com.admin.module.system.mapper.SysMenuMapper;
import com.admin.module.system.mapper.SysTenantPackageMenuMapper;
import com.admin.module.system.service.SysRoleMenuService;
import com.admin.module.system.service.SysTenantConfigService;
import com.admin.module.system.service.SysUserRoleService;
import com.admin.module.system.service.TenantPackageMenuChangeListener;
import com.admin.module.system.service.TenantPackageMenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 租户套餐菜单权限服务实现类
 */
@Service
@RequiredArgsConstructor
public class TenantPackageMenuServiceImpl implements TenantPackageMenuService {

    private final SysTenantPackageMenuMapper tenantPackageMenuMapper;
    private final SysRoleMenuService roleMenuService;
    private final SysUserRoleService userRoleService;
    private final SysTenantConfigService tenantConfigService;
    private final TenantPackageMenuChangeListener tenantPackageMenuChangeListener;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public Set<Long> getPackageMenuIds(Long packageId) {
        LambdaQueryWrapper<SysTenantPackageMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantPackageMenu::getPackageId, packageId);
        List<SysTenantPackageMenu> packageMenus = tenantPackageMenuMapper.selectList(queryWrapper);
        
        return packageMenus.stream()
                .map(SysTenantPackageMenu::getMenuId)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Long> getUserVisibleMenuIds(Long userId, Long tenantId) {
        // 如果是admin用户，返回所有菜单ID
        if (SecurityUtils.isAdmin()) {
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(SysMenu::getMenuId); // 只查询菜单ID
            List<SysMenu> allMenus = sysMenuMapper.selectList(queryWrapper);
            return allMenus.stream()
                    .map(SysMenu::getMenuId)
                    .collect(Collectors.toSet());
        }
        
        // 获取租户配置，以确定租户套餐
        SysTenantConfig tenantConfig = tenantConfigService.getTenantConfig(tenantId);
        if (tenantConfig == null || tenantConfig.getPackageId() == null) {
            // 如果租户没有套餐，返回用户角色的菜单
            return getUserRoleMenuIds(userId);
        }

        // 获取套餐菜单ID集合
        Set<Long> packageMenuIds = getPackageMenuIds(tenantConfig.getPackageId());
        
        // 获取用户角色菜单ID集合
        Set<Long> roleMenuIds = getUserRoleMenuIds(userId);
        
        // 返回交集：套餐菜单 ∩ 角色菜单
        Set<Long> visibleMenuIds = new HashSet<>(packageMenuIds);
        visibleMenuIds.retainAll(roleMenuIds);
        
        return visibleMenuIds;
    }

    /**
     * 获取用户角色的菜单ID集合
     * @param userId 用户ID
     * @return 菜单ID集合
     */
    private Set<Long> getUserRoleMenuIds(Long userId) {
        // 获取用户的角色ID集合
        List<SysUserRole> userRoleList = userRoleService.selectUserRoleList(userId);
        if (CollectionUtils.isEmpty(userRoleList)) {
            return new HashSet<>();
        }

        Set<Long> roleIds = userRoleList.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toSet());

        // 获取角色关联的菜单ID集合
        return new HashSet<>(roleMenuService.selectMenuIds(roleIds));
    }
}