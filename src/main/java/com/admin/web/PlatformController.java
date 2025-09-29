package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.Platform;
import com.admin.module.system.service.PlatformService;
import com.admin.module.system.vo.PlatformVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/platform")
public class PlatformController {

    private final PlatformService platformService;

    @Operation(summary = "平台系统分页列表")
    @GetMapping("page")
    public PageResult<PlatformVO> selectPage(PlatformVO platformVO) {
        IPage<PlatformVO> platformPage = platformService.selectPage(platformVO);
        return PageResult.success(platformPage);
    }

    @Operation(summary = "新增平台系统")
    @PreAuthorize("@pms.hasPerm('sys:platform:add')")
    @NoRepeatSubmit
    @PostMapping("/add")
    public ResultVO<Boolean> savePlatform(@RequestBody Platform platform) {
        boolean result = platformService.savePlatform(platform);
        return ResultVO.judge(result);
    }

    @Operation(summary = "平台系统信息")
    @GetMapping("/platformInfo/{id}")
    public ResultVO<Platform> getPlatformInfo(@PathVariable Long id) {
        Platform platform = platformService.getById(id);
        return ResultVO.success(platform);
    }

    @Operation(summary = "更新平台系统信息")
    @PreAuthorize("@pms.hasPerm('sys:platform:update')")
    @PutMapping("/update")
    public ResultVO<Boolean> updatePlatform(@RequestBody Platform platform) {
        boolean result = platformService.updatePlatform(platform);
        return ResultVO.judge(result);
    }

    @Operation(summary = "删除平台系统")
    @PreAuthorize("@pms.hasPerm('sys:platform:delete')")
    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> deletePlatform(@PathVariable Long id) {
        boolean result = platformService.removeById(id);
        return ResultVO.judge(result);
    }

    @Operation(summary = "平台系统首页")
    @GetMapping("/home")
    public ResultVO<List<Platform>> selectPlatformList() {
        List<Platform> platformList = platformService.selectPlatformList();
        return ResultVO.success(platformList);
    }
}
