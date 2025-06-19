package com.admin.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.form.RoleForm;
import com.admin.module.system.query.RoleQuery;
import com.admin.module.system.vo.RoleVO;
import com.admin.module.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author suYan
 * @date 2023/4/16 20:56
 */
@Tag(name = "角色接口")
@RequiredArgsConstructor
@RequestMapping("/role")
@RestController
public class SysRoleController {

    private final SysRoleService roleService;


    @Operation(summary = "角色分页列表")
    @GetMapping("/page")
    public PageResult<RoleVO> selectRolePage(RoleQuery roleQuery) {
        IPage<RoleVO> roleVOList = roleService.selectRolePage(roleQuery);
        return PageResult.success(roleVOList);
    }


    @Operation(summary = "新增角色")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:role:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> saveRole(@RequestBody RoleForm roleForm) {
        boolean result = roleService.saveRole(roleForm);
        return ResultVO.judge(result);
    }



    @Operation(summary = "编辑角色")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:role:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> updateRole(@RequestBody RoleForm roleForm) {
        boolean result = roleService.updateRole(roleForm);
        return ResultVO.judge(result);
    }


    @Operation(summary = "角色详情")
    @GetMapping("/detail/{roleId}")
    public ResultVO<RoleVO> getRoleDetail(@PathVariable Long roleId) {
        RoleVO roleVO = roleService.getRoleDetail(roleId);
        return ResultVO.success(roleVO);
    }


    @Operation(summary = "删除多个角色")
    @PreAuthorize("@pms.hasPerm('sys:role:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteRoles(@RequestBody List<Long> roleIds) {
        boolean result = roleService.deleteRoles(roleIds);
        return ResultVO.judge(result);
    }


    @Operation(summary = "启用角色")
    @PreAuthorize("@pms.hasPerm('sys:role:enable')")
    @PutMapping("/enable")
    public ResultVO<Boolean> enableRole(@RequestBody List<Long> roleIds) {
        Boolean result = roleService.enableRole(roleIds);
        return ResultVO.judge(result);
    }


    @Operation(summary = "禁用角色")
    @PreAuthorize("@pms.hasPerm('sys:role:disable')")
    @PutMapping("/disable")
    public ResultVO<Boolean> disableRole(@RequestBody List<Long> roleIds) {
        Boolean result = roleService.disableRole(roleIds);
        return ResultVO.judge(result);
    }



    @Operation(summary = "角色分配菜单，角色已分配的菜单id集合")
    @GetMapping("/{roleId}/menuIds")
    public ResultVO<List<Long>> selectMenuIds(@PathVariable("roleId") Long roleId) {
        List<Long> menuIds = roleService.selectMenuIds(roleId);
        return ResultVO.success(menuIds);
    }


    @Operation(summary = "角色下拉选项列表")
    @GetMapping("/option")
    public ResultVO<List<OptionVO>> roleTreeOption() {
        List<OptionVO> optionVOS = roleService.roleTreeOption();
        return ResultVO.success(optionVOS);
    }


    @Operation(summary = "更新新角色用户关系")
    @PreAuthorize("@pms.hasPerm('sys:role:user')")
    @PutMapping("/updateRoleUser/{roleId}")
    public ResultVO<Boolean> updateRoleUserList(@PathVariable("roleId") Long roleId, @RequestBody List<Long> userIds) {
        boolean result = roleService.updateRoleUsers(roleId, userIds);
        return ResultVO.judge(result);
    }

}
