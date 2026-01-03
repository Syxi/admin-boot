package com.admin.web;

import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysTenantConfig;
import com.admin.module.system.service.SysTenantConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 租户配置管理
 */
@Tag(name = "租户配置管理")
@RestController
@RequestMapping("/tenantConfig")
@RequiredArgsConstructor
public class SysTenantConfigController {

    private final SysTenantConfigService sysTenantConfigService;

    /**
     * 获取租户配置信息
     */
    @Operation(summary = "获取租户配置信息")
    @GetMapping("/config/{tenantId}")
    public ResultVO<SysTenantConfig> getTenantConfig(@PathVariable Long tenantId) {
        SysTenantConfig config = sysTenantConfigService.getTenantConfig(tenantId);
        return ResultVO.success(config);
    }

    /**
     * 更新租户用户数
     */
    @Operation(summary = "更新租户用户数")
    @PutMapping("/updateUserCount/{tenantId}/{increment}")
    public ResultVO<Void> updateTenantUserCount(@PathVariable Long tenantId, @PathVariable int increment) {
        sysTenantConfigService.updateTenantUserCount(tenantId, increment);
        return ResultVO.success();
    }
}