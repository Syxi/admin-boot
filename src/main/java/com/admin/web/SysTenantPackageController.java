package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysTenantPackage;
import com.admin.module.system.query.TenantPackageQuery;
import com.admin.module.system.service.SysTenantPackageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户套餐管理
 */
@Tag(name = "租户套餐管理")
@RestController
@RequestMapping("/tenantPackage")
@RequiredArgsConstructor
public class SysTenantPackageController {

    private final SysTenantPackageService sysTenantPackageService;

    /**
     * 获取租户套餐分页列表
     */
    @Operation(summary = "获取租户套餐分页列表")
    @GetMapping("/page")
    public PageResult<SysTenantPackage> selectTenantPackagePage(TenantPackageQuery tenantPackageQuery) {
        IPage<SysTenantPackage> tenantPackageList = sysTenantPackageService.selectTenantPackagePage(tenantPackageQuery);
        return PageResult.success(tenantPackageList);
    }

    /**
     * 获取租户套餐列表
     */
    @Operation(summary = "获取租户套餐列表")
    @GetMapping("/list")
    public ResultVO<List<SysTenantPackage>> list() {
        List<SysTenantPackage> packages = sysTenantPackageService.list();
        return ResultVO.success(packages);
    }

    /**
     * 获取租户套餐详情
     */
    @Operation(summary = "获取租户套餐详情")
    @GetMapping("/detail/{id}")
    public ResultVO<SysTenantPackage> detail(@PathVariable Long id) {
        SysTenantPackage tenantPackage = sysTenantPackageService.getById(id);
        return ResultVO.success(tenantPackage);
    }

    /**
     * 新增租户套餐
     */
    @Operation(summary = "新增租户套餐")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:tenantPackage:add')")
    @PostMapping("/add")
    public ResultVO<Void> add(@RequestBody SysTenantPackage tenantPackage) {
        sysTenantPackageService.save(tenantPackage);
        return ResultVO.success();
    }

    /**
     * 更新租户套餐
     */
    @Operation(summary = "更新租户套餐")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:tenantPackage:edit')")
    @PutMapping("/update")
    public ResultVO<Void> update(@RequestBody SysTenantPackage tenantPackage) {
        sysTenantPackageService.updateById(tenantPackage);
        return ResultVO.success();
    }

    /**
     * 删除租户套餐
     */
    @Operation(summary = "删除租户套餐")
    @PreAuthorize("@pms.hasPerm('sys:tenantPackage:delete')")
    @DeleteMapping("/delete/{ids}")
    public ResultVO<Void> delete(@PathVariable String ids) {
        // 将逗号分隔的ID字符串转换为Long列表
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        sysTenantPackageService.removeBatchByIds(idList);
        return ResultVO.success();
    }
}