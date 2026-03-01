package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.workflow.form.WorkflowDefinitionForm;
import com.admin.module.workflow.query.WorkflowDefinitionQuery;
import com.admin.module.workflow.service.WorkflowDefinitionService;
import com.admin.module.workflow.vo.WorkflowDefinitionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程定义控制器
 */
@Slf4j
@Tag(name = "流程定义接口")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/definition")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService workflowDefinitionService;

    @Operation(summary = "流程定义分页列表")
    @GetMapping("/page")
    public PageResult<WorkflowDefinitionVO> page(WorkflowDefinitionQuery query) {
        return workflowDefinitionService.selectDefinitionPage(query);
    }

    @Operation(summary = "流程定义详情")
    @GetMapping("/{id}")
    public ResultVO<WorkflowDefinitionVO> getById(@PathVariable Long id) {
        WorkflowDefinitionVO vo = workflowDefinitionService.getDefinitionById(id);
        return ResultVO.success(vo);
    }

    @Operation(summary = "获取最新版本流程定义")
    @GetMapping("/latest/{processKey}")
    public ResultVO<WorkflowDefinitionVO> getLatestByKey(@PathVariable String processKey) {
        WorkflowDefinitionVO vo = workflowDefinitionService.getLatestByKey(processKey);
        return ResultVO.success(vo);
    }

    @Operation(summary = "新增流程定义")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:definition:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> add(@Valid @RequestBody WorkflowDefinitionForm form) {
        return workflowDefinitionService.saveDefinition(form);
    }

    @Operation(summary = "更新流程定义")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:definition:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> edit(@Valid @RequestBody WorkflowDefinitionForm form) {
        return workflowDefinitionService.updateDefinition(form);
    }

    @Operation(summary = "删除流程定义")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:definition:delete')")
    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> delete(@PathVariable Long id) {
        return workflowDefinitionService.deleteDefinition(id);
    }

    @Operation(summary = "发布流程定义")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:definition:deploy')")
    @PostMapping("/deploy/{id}")
    public ResultVO<Boolean> deploy(@PathVariable Long id) {
        return workflowDefinitionService.deployDefinition(id);
    }

    @Operation(summary = "停用流程定义")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:definition:suspend')")
    @PostMapping("/suspend/{id}")
    public ResultVO<Boolean> suspend(@PathVariable Long id) {
        return workflowDefinitionService.suspendDefinition(id);
    }

    @Operation(summary = "激活流程定义")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:definition:activate')")
    @PostMapping("/activate/{id}")
    public ResultVO<Boolean> activate(@PathVariable Long id) {
        return workflowDefinitionService.activateDefinition(id);
    }

    @Operation(summary = "复制流程定义")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:definition:copy')")
    @PostMapping("/copy/{id}")
    public ResultVO<Boolean> copy(@PathVariable Long id) {
        return workflowDefinitionService.copyDefinition(id);
    }

    @Operation(summary = "获取流程分类列表")
    @GetMapping("/categories")
    public ResultVO<List<String>> getCategories() {
        return ResultVO.success(workflowDefinitionService.getCategoryList());
    }

    @Operation(summary = "获取流程XML")
    @GetMapping("/xml/{id}")
    public ResultVO<String> getXml(@PathVariable Long id) {
        String xml = workflowDefinitionService.getProcessXml(id);
        return ResultVO.success(xml);
    }

    @Operation(summary = "获取流程图")
    @GetMapping("/diagram/{id}")
    public ResultVO<String> getDiagram(@PathVariable Long id) {
        String svg = workflowDefinitionService.getProcessDiagram(id);
        return ResultVO.success(svg);
    }
}
