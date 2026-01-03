package com.admin.web;

import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysTenantPackage;
import com.admin.module.system.service.SysTenantPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/add")
    public ResultVO<Void> add(@RequestBody SysTenantPackage tenantPackage) {
        sysTenantPackageService.save(tenantPackage);
        return ResultVO.success();
    }

    /**
     * 更新租户套餐
     */
    @Operation(summary = "更新租户套餐")
    @PutMapping("/update")
    public ResultVO<Void> update(@RequestBody SysTenantPackage tenantPackage) {
        sysTenantPackageService.updateById(tenantPackage);
        return ResultVO.success();
    }

    /**
     * 删除租户套餐
     */
    @Operation(summary = "删除租户套餐")
    @DeleteMapping("/delete/{id}")
    public ResultVO<Void> delete(@PathVariable Long id) {
        sysTenantPackageService.removeById(id);
        return ResultVO.success();
    }
}