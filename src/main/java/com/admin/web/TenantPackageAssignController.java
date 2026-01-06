package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysTenantConfig;
import com.admin.module.system.service.SysTenantConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 租户套餐分配管理
 */
@Tag(name = "租户套餐分配管理")
@RestController
@RequestMapping("/tenantPackage/assign")
@RequiredArgsConstructor
public class TenantPackageAssignController {

    private final SysTenantConfigService sysTenantConfigService;

    /**
     * 为租户分配套餐
     */
    @Operation(summary = "为租户分配套餐")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:tenantPackage:assign')")
    @PostMapping("/assign")
    public ResultVO<Void> assignPackageToTenant(@RequestParam Long tenantId, @RequestParam Long packageId) {
        boolean result = sysTenantConfigService.assignPackageToTenant(tenantId, packageId);
        return ResultVO.judge(result);
    }

    /**
     * 获取租户当前套餐信息
     */
    @Operation(summary = "获取租户当前套餐信息")
    @GetMapping("/current/{tenantId}")
    public ResultVO<SysTenantConfig> getCurrentTenantConfig(@PathVariable Long tenantId) {
        SysTenantConfig config = sysTenantConfigService.getTenantCurrentConfig(tenantId);
        return ResultVO.success(config);
    }
}