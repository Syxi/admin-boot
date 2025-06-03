package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.ResultVO;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.form.OrganizationForm;
import com.admin.module.system.vo.OrganizationVO;
import com.admin.module.system.service.SysOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "组织机构")
@RestController
@RequestMapping("/api/organization")
@AllArgsConstructor
public class SysOrganizationController {

    private final SysOrganizationService sysOrganizationService;

    @Operation(summary = "组织树")
    @GetMapping("/tree")
    public ResultVO<List<OrganizationVO>> organizationTree(String keyWord) {
        List<OrganizationVO> organizationList = sysOrganizationService.organizationTree(keyWord);
        return ResultVO.success(organizationList);
    }


    @Operation(summary = "组织下拉列表")
    @GetMapping("/option")
    public ResultVO<List<OptionVO>> organizationTreeOptions() {
        List<OptionVO> optionVOList = sysOrganizationService.organizationTreeOptions();
        return ResultVO.success(optionVOList);
    }


    @Operation(summary = "新增组织")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:organ:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> saveOrganization(@RequestBody OrganizationForm organizationForm) {
        boolean result = sysOrganizationService.saveOrganization(organizationForm);
        return ResultVO.judge(result);
    }


    @Operation(summary = "获取组织信息")
    @GetMapping("/detail/{id}")
    public ResultVO<OrganizationForm> getOrganizationDetail(@PathVariable("id") Long id) {
        OrganizationForm organizationForm = sysOrganizationService.getOrganizationDetail(id);
        return ResultVO.success(organizationForm);
    }

    @Operation(summary = "编辑组织")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:organ:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> updateOrganization(@RequestBody OrganizationForm organizationForm) {
        boolean result = sysOrganizationService.updateOrganization(organizationForm);
        return ResultVO.judge(result);
    }



    @Operation(summary = "删除组织")
    @PreAuthorize("@pms.hasPerm('sys:organ:delete')")
    @DeleteMapping("/{id}")
    public ResultVO<Boolean> deleteOrganization(@PathVariable("id") Long id) {
        boolean result = sysOrganizationService.deleteOrganization(id);
        return ResultVO.judge(result);
    }
}
