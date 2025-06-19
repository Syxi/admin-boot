package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysSwitchConfig;
import com.admin.module.system.query.SysSwitchConfigQuery;
import com.admin.module.system.service.SysSwitchConfigService;
import com.admin.module.system.vo.SysSwitchConfigVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统业务配置开关")
@RequiredArgsConstructor
@RequestMapping("/sysSwitchConfig")
@RestController
public class SysSwitchConfigController {

    private final SysSwitchConfigService sysSwitchConfigService;

    @Operation(summary = "系统业务配置开关分页列表")
    @GetMapping("/page")
    public PageResult<SysSwitchConfigVO> selectSysSwitchConfigPage(SysSwitchConfigQuery configQuery) {
        IPage<SysSwitchConfigVO> sysSwitchConfigIPage = sysSwitchConfigService.selectSysSwitchConfigPage(configQuery);
        return PageResult.success(sysSwitchConfigIPage);
    }


    @Operation(summary = "获取系统配置信息")
    @GetMapping("/{id}")
    public ResultVO<SysSwitchConfig> getSysSwitchConfig(@PathVariable Long id) {
        SysSwitchConfig sysSwitchConfig = sysSwitchConfigService.getById(id);
        return ResultVO.success(sysSwitchConfig);
    }


    @Operation(summary = "更新系统配置业务信息")
    @NoRepeatSubmit
    @PutMapping("/update")
    public ResultVO<Boolean> updateSysSwitchConfig(@RequestBody SysSwitchConfig sysSwitchConfig) {
        boolean result = sysSwitchConfigService.updateSysSwitchConfig(sysSwitchConfig);
        return ResultVO.success(result);
    }


    @Operation(summary = "启动或关闭系统配置")
    @PutMapping("/update/{id}")
    public ResultVO<Boolean> updateSwitchConfigStatus(@PathVariable Long id, @RequestParam String configKey,
                                                      @RequestParam Boolean configValue) {
        boolean result = sysSwitchConfigService.updateStatus(id, configKey, configValue);
        return ResultVO.success(result);
    }

}
