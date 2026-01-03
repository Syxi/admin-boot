package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.dto.TenantUserForm;
import com.admin.module.system.entity.SysTenant;
import com.admin.module.system.query.TenantQuery;
import com.admin.module.system.service.SysTenantService;
import com.admin.module.system.vo.TransferVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "租户接口")
@RequiredArgsConstructor
@RequestMapping("/tenant")
@RestController
public class TenantController {

    private final SysTenantService tenantService;

    @Operation(summary = "租户分页列表")
    @GetMapping("/page")
    public PageResult<SysTenant> selectTenantPage(TenantQuery tenantQuery) {
        IPage<SysTenant> tenantList = tenantService.selectUserPage(tenantQuery);
        return PageResult.success(tenantList);
    }


    @Operation(summary = "新增租户")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:tenant:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> addTenant(@RequestBody SysTenant tenant) {
        boolean result = tenantService.saveTenant(tenant);
        return ResultVO.judge(result);
    }



    @Operation(summary = "更新租户")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:tenant:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> editUser(@RequestBody SysTenant tenant) {
        boolean result = tenantService.updateTenant(tenant);
        return ResultVO.judge(result);
    }


    @Operation(summary = "获取租户详情")
    @GetMapping("/detail/{id}")
    public ResultVO<SysTenant> getUserDetail(@PathVariable("id") @NotNull(message = "id不能为空") Long id) {
        SysTenant tenant = tenantService.getTenantDetail(id);
        return ResultVO.success(tenant);
    }




    @Operation(summary = "批量删除租户")
    @PreAuthorize("@pms.hasPerm('sys:tenant:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> batchRemoveTenant(@RequestBody List<Long> ids) {
        boolean result = tenantService.deleteBatchTenants(ids);
        return ResultVO.judge(result);
    }

    @Operation(summary = "给租户分配用户")
    @PreAuthorize("@pms.hasPerm('sys:tenant:user:add')")
    @PostMapping("/addUser/{id}")
    public ResultVO<Boolean> updateTenantUsers(@RequestBody List<Long> userIds, @PathVariable("id") Long id) {
        boolean result = tenantService.updateTenantUsers(userIds, id);
        return ResultVO.judge(result);
    }

    @Operation(summary = "获取租户下所有用户")
    @GetMapping("/usersInTenant/{id}")
    public ResultVO<List<TransferVO>> selectUsersInTenant(@PathVariable("id") Long id) {
        List<TransferVO> transferVOS = tenantService.selectUsersInTenant(id);
        return ResultVO.success(transferVOS);
    }

    @Operation(summary = "未分配租户的所有用户")
    @GetMapping("/userNotInTenant/{id}")
    public ResultVO<List<TransferVO>> selectUsersNotInTenant(@PathVariable("id") Long id) {
        List<TransferVO> transferVOS = tenantService.selectUsersNotInTenant(id);
        return ResultVO.success(transferVOS);
    }


    /**
     * 切换租户
     */
    @Operation(summary = "切换租户")
    @PutMapping("/switch")
    @PreAuthorize("hasAuthority('system:tenant:switch')")
    public ResultVO<Void> switchTenant(@RequestBody TenantUserForm form) {
        Long targetTenantId = form.getTenantId();

        if (targetTenantId == null) {
            return ResultVO.error("租户ID不能为空");
        }

        // 验证用户是否有权访问目标租户
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return ResultVO.error("用户未登录");
        }

        boolean hasAccess = tenantService.checkUserTenantAccess(userId, targetTenantId);
        if (!hasAccess) {
            return ResultVO.error("无权访问该租户");
        }

        // 实际的租户切换逻辑会在JWT Token刷新或用户信息重新加载时生效
        // 当前实现主要验证用户权限
        return ResultVO.success();
    }

    /**
     * 获取用户可访问的租户列表
     */
    @Operation(summary = "获取用户可访问的租户列表")
    @GetMapping("/userTenants")
    @PreAuthorize("hasAuthority('system:tenant:list')")
    public ResultVO<List<TenantUserForm>> getUserTenants() {
        // 实现获取用户租户列表逻辑
        return ResultVO.success(tenantService.getUserTenants());
    }

}
