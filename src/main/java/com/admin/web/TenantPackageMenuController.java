package com.admin.web;

import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysTenantPackageMenu;
import com.admin.module.system.service.SysTenantPackageMenuService;
import com.admin.module.system.service.TenantPackageMenuChangeListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户套餐菜单授权管理
 */
@Tag(name = "租户套餐菜单授权管理")
@RestController
@RequestMapping("/tenantPackage/menu")
@RequiredArgsConstructor
public class TenantPackageMenuController {

    private final SysTenantPackageMenuService sysTenantPackageMenuService;
    private final TenantPackageMenuChangeListener tenantPackageMenuChangeListener;

    /**
     * 获取套餐已分配的菜单列表
     */
    @Operation(summary = "获取套餐已分配的菜单列表")
    @GetMapping("/assignedMenus/{packageId}")
    @PreAuthorize("@pms.hasPerm('sys:tenantPackage:menu')")
    public ResultVO<List<Long>> getAssignedMenus(@PathVariable Long packageId) {
        // 查询套餐已分配的菜单ID列表
        LambdaQueryWrapper<SysTenantPackageMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantPackageMenu::getPackageId, packageId);
        List<SysTenantPackageMenu> packageMenus = sysTenantPackageMenuService.getBaseMapper()
            .selectList(queryWrapper);
        
        List<Long> menuIds = packageMenus.stream()
            .map(SysTenantPackageMenu::getMenuId)
            .toList();
        
        return ResultVO.success(menuIds);
    }

    /**
     * 为套餐分配菜单权限
     */
    @Operation(summary = "为套餐分配菜单权限")
    @PostMapping("/assignMenus/{packageId}")
    @PreAuthorize("@pms.hasPerm('sys:tenantPackage:menu')")
    public ResultVO<Void> assignMenusToPackage(@PathVariable Long packageId, @RequestBody List<Long> menuIds) {
        // 先删除该套餐现有的所有菜单权限
        LambdaQueryWrapper<SysTenantPackageMenu> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysTenantPackageMenu::getPackageId, packageId);
        sysTenantPackageMenuService.getBaseMapper()
            .delete(deleteWrapper);
        
        // 批量插入新的菜单权限
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysTenantPackageMenu packageMenu = new SysTenantPackageMenu();
                packageMenu.setPackageId(packageId);
                packageMenu.setMenuId(menuId);
                sysTenantPackageMenuService.save(packageMenu);
            }
        }
        
        // 触发套餐菜单变更事件，更新使用该套餐的租户权限缓存
        tenantPackageMenuChangeListener.onPackageMenuChanged(packageId);
        
        return ResultVO.success();
    }
}