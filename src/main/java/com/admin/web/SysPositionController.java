package com.admin.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.form.PositionForm;
import com.admin.module.system.query.PositionQuery;
import com.admin.module.system.vo.PositionVO;
import com.admin.module.system.service.SysPositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "岗位管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sysPosition")
public class SysPositionController {

    private final SysPositionService sysPositionService;

    @Operation(summary = "岗位分页列表")
    @GetMapping("/page")
    public PageResult<PositionVO> selectSysPositionPage(PositionQuery positionQuery) {
        IPage<PositionVO> sysPositionIPage = sysPositionService.selectSysPositionPage(positionQuery);
        return PageResult.success(sysPositionIPage);
    }


    @Operation(summary = "新增岗位")
    @PreAuthorize("@pms.hasPerm('sys:position:add')")
    @NoRepeatSubmit
    @PostMapping("/add")
    public ResultVO<Boolean> saveSysPosition(@RequestBody PositionForm positionForm) {
        boolean result = sysPositionService.saveSysPosition(positionForm);
        return ResultVO.success();
    }

    @Operation(summary = "岗位详情信息")
    @GetMapping("/detail/{id}")
    public ResultVO<PositionForm> getSysPositionDetail(@PathVariable("id") Long id) {
        PositionForm positionForm = sysPositionService.getSysPositionDetail(id);
        return ResultVO.success(positionForm);
    }


    @Operation(summary = "更新岗位")
    @PreAuthorize("@pms.hasPerm('sys:position:edit')")
    @NoRepeatSubmit
    @PutMapping("/update")
    public ResultVO<Boolean> updateSysPosition(@RequestBody PositionForm positionForm) {
        boolean result = sysPositionService.updateSysPosition(positionForm);
        return ResultVO.success(result);
    }


    @Operation(summary = "删除岗位")
    @PreAuthorize("@pms.hasPerm('sys:position:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteSysPosition(@RequestBody List<Long> ids) {
        boolean result = sysPositionService.deleteSysPosition(ids);
        return ResultVO.success(result);
    }



}
