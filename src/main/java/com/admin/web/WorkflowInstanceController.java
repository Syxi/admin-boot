package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.workflow.form.StartProcessForm;
import com.admin.module.workflow.query.WorkflowInstanceQuery;
import com.admin.module.workflow.service.WorkflowInstanceService;
import com.admin.module.workflow.vo.WorkflowInstanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 流程实例控制器
 */
@Slf4j
@Tag(name = "流程实例接口")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/instance")
public class WorkflowInstanceController {

    private final WorkflowInstanceService workflowInstanceService;

    @Operation(summary = "流程实例分页列表")
    @GetMapping("/page")
    public PageResult<WorkflowInstanceVO> page(WorkflowInstanceQuery query) {
        return workflowInstanceService.selectInstancePage(query);
    }

    @Operation(summary = "流程实例详情")
    @GetMapping("/{id}")
    public ResultVO<WorkflowInstanceVO> getById(@PathVariable Long id) {
        WorkflowInstanceVO vo = workflowInstanceService.getInstanceById(id);
        return ResultVO.success(vo);
    }

    @Operation(summary = "根据业务Key获取流程实例")
    @GetMapping("/business/{businessKey}")
    public ResultVO<WorkflowInstanceVO> getByBusinessKey(@PathVariable String businessKey) {
        WorkflowInstanceVO vo = workflowInstanceService.getInstanceByBusinessKey(businessKey);
        return ResultVO.success(vo);
    }

    @Operation(summary = "启动流程实例")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:instance:start')")
    @PostMapping("/start")
    public ResultVO<WorkflowInstanceVO> start(@Valid @RequestBody StartProcessForm form) {
        return workflowInstanceService.startProcessInstance(form);
    }

    @Operation(summary = "终止流程实例")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:instance:terminate')")
    @PostMapping("/terminate/{id}")
    public ResultVO<Boolean> terminate(@PathVariable Long id, @RequestParam String reason) {
        return workflowInstanceService.terminateInstance(id, reason);
    }

    @Operation(summary = "删除流程实例")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:instance:delete')")
    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> delete(@PathVariable Long id) {
        return workflowInstanceService.deleteInstance(id);
    }

    @Operation(summary = "撤回流程实例")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:instance:revoke')")
    @PostMapping("/revoke/{id}")
    public ResultVO<Boolean> revoke(@PathVariable Long id, @RequestParam String reason) {
        return workflowInstanceService.revokeInstance(id, reason);
    }

    @Operation(summary = "挂起流程实例")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:instance:suspend')")
    @PostMapping("/suspend/{id}")
    public ResultVO<Boolean> suspend(@PathVariable Long id) {
        return workflowInstanceService.suspendInstance(id);
    }

    @Operation(summary = "激活流程实例")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:instance:activate')")
    @PostMapping("/activate/{id}")
    public ResultVO<Boolean> activate(@PathVariable Long id) {
        return workflowInstanceService.activateInstance(id);
    }

    @Operation(summary = "获取流程进度")
    @GetMapping("/progress/{id}")
    public ResultVO<Map<String, Object>> getProgress(@PathVariable Long id) {
        Map<String, Object> progress = workflowInstanceService.getProcessProgress(id);
        return ResultVO.success(progress);
    }

    @Operation(summary = "我发起的流程")
    @GetMapping("/my-started")
    public PageResult<WorkflowInstanceVO> myStarted(WorkflowInstanceQuery query) {
        return workflowInstanceService.getMyStartedInstances(query);
    }

    @Operation(summary = "获取流程变量")
    @GetMapping("/variables/{id}")
    public ResultVO<Map<String, Object>> getVariables(@PathVariable Long id) {
        Map<String, Object> variables = workflowInstanceService.getVariables(id);
        return ResultVO.success(variables);
    }

    @Operation(summary = "更新流程变量")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:instance:updateVariables')")
    @PutMapping("/variables/{id}")
    public ResultVO<Boolean> updateVariables(@PathVariable Long id, @RequestBody Map<String, Object> variables) {
        return workflowInstanceService.updateVariables(id, variables);
    }
}
