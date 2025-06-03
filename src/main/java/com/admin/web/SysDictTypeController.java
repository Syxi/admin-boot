package com.admin.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysDictType;
import com.admin.module.system.query.DictTypeQuery;
import com.admin.module.system.service.SysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author suYan
 * @date 2023/4/22 13:20
 */
@Tag(name = "字典类型接口")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dictType")
public class SysDictTypeController {

    private final SysDictTypeService dictTypeService;

    @Operation(summary = "字典类型分页列表")
    @GetMapping("/page")
    public PageResult<SysDictType> dictTypePage(DictTypeQuery dictTypeQuery) {
        IPage<SysDictType> dictTypeVOList = dictTypeService.selectDictTypePage(dictTypeQuery);
        return PageResult.success(dictTypeVOList);
    }



    @Operation(summary = "字典详情")
    @GetMapping("/detail/{id}")
    public ResultVO getDictTypeDetail(@PathVariable("id") Long id) {
        SysDictType dictType = dictTypeService.getDictTypeDetail(id);
        return ResultVO.success(dictType);
    }



    @Operation(summary = "新增字典类型")
    @PreAuthorize("@pms.hasPerm('sys:dictType:add')")
    @NoRepeatSubmit
    @PostMapping("/add")
    public ResultVO<Boolean> saveDictType(@RequestBody SysDictType dictType) {
        boolean result = dictTypeService.saveDictType(dictType);
        return ResultVO.judge(result);
    }


    @Operation(summary = "编辑字典类型")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:dictType:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> updateDictType(@RequestBody SysDictType dictType) {
        boolean result = dictTypeService.updateDictType(dictType);
        return ResultVO.judge(result);
    }



    @Operation(summary = "删除字典")
    @PreAuthorize("@pms.hasPerm('sys:dictType:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteDictType(@RequestBody List<Long> ids) {
        boolean result = dictTypeService.deleteDictTypes(ids);
        return ResultVO.judge(result);
    }

}
