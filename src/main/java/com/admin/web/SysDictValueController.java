package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysDictValue;
import com.admin.module.system.query.DictValueQuery;
import com.admin.module.system.service.SysDictValueService;
import com.admin.module.system.vo.OptionVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author suYan
 * @date 2023/4/22 19:52
 */
@Tag(name = "字典项")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dictValue")
public class SysDictValueController {

    private final SysDictValueService dictValueService;


    @Operation(summary = "字典项分页列表")
    @GetMapping("/page")
    public PageResult<SysDictValue> dictValuePage(DictValueQuery dictValueQuery) {
        IPage<SysDictValue> dictValueList = dictValueService.selectDictValuePage(dictValueQuery);
        return PageResult.success(dictValueList);

    }


    @Operation(summary = "字典项详情" )
    @GetMapping("/detail/{id}")
    public ResultVO<SysDictValue> getDictValueDetail(@PathVariable("id") Long id) {
        SysDictValue dictValue= dictValueService.getDictValueDetail(id);
        return ResultVO.success(dictValue);
    }


    @Operation(summary = "添加字典项")
    @NoRepeatSubmit
    @PostMapping("/add")
    public ResultVO<Boolean> saveDictValue(@RequestBody SysDictValue dictValue) {
        boolean result = dictValueService.selectDictValue(dictValue);
        return ResultVO.judge(result);
    }


    @Operation(summary = "编辑字典项")
    @NoRepeatSubmit
    @PutMapping("/edit")
    public ResultVO<Boolean> updateDictValue(@RequestBody SysDictValue dictValue) {
        boolean result = dictValueService.updateDictValue(dictValue);
        return ResultVO.judge(result);
    }


    @Operation(summary = "批量删除字典项")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteDictValue(@RequestBody List<Long> ids) {
        boolean result = dictValueService.deleteDictValue(ids);
        return ResultVO.judge(result);
    }


    @Operation(summary = "字典下拉树")
    @GetMapping("/option/{typeCode}")
    public ResultVO<List<OptionVO>>  dictValueTreeOptions(@PathVariable("typeCode") String typeCode){
        List<OptionVO> optionVOList = dictValueService.treeOptions(typeCode);
        return ResultVO.success(optionVOList);
    }


}
