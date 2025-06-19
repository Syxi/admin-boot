package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.ResultVO;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.form.MenuForm;
import com.admin.module.system.query.MenuQuery;
import com.admin.module.system.service.SysMenuService;
import com.admin.module.system.vo.MenuVO;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.vo.RouteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author suYan
 * @date 2023/4/2 18:56
 */
@Tag(name = "菜单接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/menu")
public class SysMenuController {

    private final SysMenuService menuService;


    @Operation(summary = "菜单树")
    @GetMapping("/page")
    public ResultVO<List<MenuVO>> menuTree(MenuQuery menuQuery) {
        List<MenuVO> menuVOList = menuService.menuTree(menuQuery);
        return ResultVO.success(menuVOList);
    }


    @Operation(summary = "菜单下拉树")
    @GetMapping("/option")
    public ResultVO<List<OptionVO>> menuTreeOption() {
        List<OptionVO> menus = menuService.menuTreeOption();
        return ResultVO.success(menus);
    }


    @Operation(summary = "路由列表, 登录系统，生成菜单")
    @GetMapping("/routes")
    public ResultVO<List<RouteVO>> selectRoutes() {
        Set<String> roleCode = SecurityUtils.getRoleCodes();
        List<RouteVO> routeVOList = menuService.selectRouteList(roleCode);
        return ResultVO.success(routeVOList);
    }

    @Operation(summary = "新增菜单")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:menu:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> saveMenu(@RequestBody MenuForm menuForm) {
        boolean result = menuService.saveMenu(menuForm);
        return ResultVO.judge(result);
    }



    @Operation(summary = "编辑菜单")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:menu:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> updateMenu(@RequestBody MenuForm menuForm) {
        boolean result = menuService.updateMenu(menuForm);
        return ResultVO.judge(result);
    }


    @Operation(summary = "获取菜单数据，编辑菜单时使用")
    @GetMapping("/detail/{menuId}")
    public ResultVO<MenuForm> getMenuDetail(@PathVariable Long menuId) {
        MenuForm menuForm = menuService.getMenuDetail(menuId);
        return ResultVO.success(menuForm);
    }


    @Operation(summary = "删除菜单")
    @PreAuthorize("@pms.hasPerm('sys:menu:delete')")
    @DeleteMapping("/delete/{menuId}")
    public ResultVO<Boolean> deleteMenu(@PathVariable Long menuId) {
        boolean result = menuService.deleteMenu(menuId);
        return ResultVO.judge(result);
    }


    @Operation(summary = "给角色分配菜单，更新角色分配的menuIds")
    @PreAuthorize("@pms.hasPerm('sys:role:menu')")
    @PutMapping("/{roleId}/menus")
    public ResultVO<Boolean> updateRoleMenus(@PathVariable("roleId") Long roleId, @RequestBody List<Long> menuIds) {
        boolean result = menuService.updateRoleMenuList(roleId, menuIds);
        return ResultVO.judge(result);
    }







}
