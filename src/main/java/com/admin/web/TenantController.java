package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
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

}
