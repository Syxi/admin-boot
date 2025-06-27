package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.ResultVO;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.form.DeptForm;
import com.admin.module.system.vo.DeptVO;
import com.admin.module.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门机构")
@RestController
@RequestMapping("/dept")
@AllArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;

    @Operation(summary = "部门树")
    @GetMapping("/tree")
    public ResultVO<List<DeptVO>> deptTree(String keyWord) {
        List<DeptVO> deptList = sysDeptService.deptTree(keyWord);
        return ResultVO.success(deptList);
    }


    @Operation(summary = "部门下拉列表")
    @GetMapping("/option")
    public ResultVO<List<OptionVO>> deptTreeOptions() {
        List<OptionVO> optionVOList = sysDeptService.deptTreeOptions();
        return ResultVO.success(optionVOList);
    }


    @Operation(summary = "新增部门")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:dept:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> saveDept(@RequestBody DeptForm deptForm) {
        boolean result = sysDeptService.saveDept(deptForm);
        return ResultVO.judge(result);
    }


    @Operation(summary = "获取部门信息")
    @GetMapping("/detail/{id}")
    public ResultVO<DeptForm> getDeptDetail(@PathVariable("id") Long id) {
        DeptForm deptForm = sysDeptService.getDeptDetail(id);
        return ResultVO.success(deptForm);
    }

    @Operation(summary = "编辑部门")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:dept:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> updateDept(@RequestBody DeptForm deptForm) {
        boolean result = sysDeptService.updateDept(deptForm);
        return ResultVO.judge(result);
    }



    @Operation(summary = "删除部门")
    @PreAuthorize("@pms.hasPerm('sys:dept:delete')")
    @DeleteMapping("/{id}")
    public ResultVO<Boolean> deleteDept(@PathVariable("id") Long id) {
        boolean result = sysDeptService.deleteDept(id);
        return ResultVO.judge(result);
    }
}
