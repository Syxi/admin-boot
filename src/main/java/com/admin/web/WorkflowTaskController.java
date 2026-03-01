package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.common.security.SecurityUtils;
import com.admin.module.workflow.form.TaskCompleteForm;
import com.admin.module.workflow.form.TaskDelegateForm;
import com.admin.module.workflow.form.TaskTransferForm;
import com.admin.module.workflow.query.WorkflowTaskQuery;
import com.admin.module.workflow.service.WorkflowTaskService;
import com.admin.module.workflow.vo.WorkflowTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程任务控制器
 */
@Slf4j
@Tag(name = "流程任务接口")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/task")
public class WorkflowTaskController {

    private final WorkflowTaskService workflowTaskService;

    @Operation(summary = "待办任务分页列表")
    @GetMapping("/todo")
    public PageResult<WorkflowTaskVO> todoList(WorkflowTaskQuery query) {
        return workflowTaskService.selectTodoPage(query);
    }

    @Operation(summary = "已办任务分页列表")
    @GetMapping("/done")
    public PageResult<WorkflowTaskVO> doneList(WorkflowTaskQuery query) {
        return workflowTaskService.selectDonePage(query);
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public ResultVO<WorkflowTaskVO> getById(@PathVariable Long id) {
        WorkflowTaskVO vo = workflowTaskService.getTaskById(id);
        return ResultVO.success(vo);
    }

    @Operation(summary = "审批通过")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:task:approve')")
    @PostMapping("/complete")
    public ResultVO<Boolean> complete(@Valid @RequestBody TaskCompleteForm form) {
        return workflowTaskService.completeTask(form);
    }

    @Operation(summary = "审批驳回")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:task:reject')")
    @PostMapping("/reject")
    public ResultVO<Boolean> reject(@Valid @RequestBody TaskCompleteForm form) {
        return workflowTaskService.rejectTask(form);
    }

    @Operation(summary = "转办任务")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:task:transfer')")
    @PostMapping("/transfer")
    public ResultVO<Boolean> transfer(@Valid @RequestBody TaskTransferForm form) {
        return workflowTaskService.transferTask(form);
    }

    @Operation(summary = "委派任务")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:task:delegate')")
    @PostMapping("/delegate")
    public ResultVO<Boolean> delegate(@Valid @RequestBody TaskDelegateForm form) {
        return workflowTaskService.delegateTask(form);
    }

    @Operation(summary = "撤回任务")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:task:revoke')")
    @PostMapping("/revoke/{taskId}")
    public ResultVO<Boolean> revoke(@PathVariable Long taskId, @RequestParam String reason) {
        return workflowTaskService.revokeTask(taskId, reason);
    }

    @Operation(summary = "获取任务表单数据")
    @GetMapping("/form-data/{taskId}")
    public ResultVO<Map<String, Object>> getFormData(@PathVariable Long taskId) {
        Map<String, Object> formData = workflowTaskService.getTaskFormData(taskId);
        return ResultVO.success(formData);
    }

    @Operation(summary = "保存任务表单数据")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:task:saveForm')")
    @PostMapping("/form-data/{taskId}")
    public ResultVO<Boolean> saveFormData(@PathVariable Long taskId, @RequestBody Map<String, Object> formData) {
        return workflowTaskService.saveTaskFormData(taskId, formData);
    }

    @Operation(summary = "获取可退回节点")
    @GetMapping("/back-nodes/{taskId}")
    public ResultVO<List<Map<String, Object>>> getBackNodes(@PathVariable Long taskId) {
        List<Map<String, Object>> nodes = workflowTaskService.getBackNodes(taskId);
        return ResultVO.success(nodes);
    }

    @Operation(summary = "退回任务")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:task:back')")
    @PostMapping("/back/{taskId}")
    public ResultVO<Boolean> back(@PathVariable Long taskId, 
                                   @RequestParam String targetNodeId,
                                   @RequestParam String reason) {
        return workflowTaskService.backTask(taskId, targetNodeId, reason);
    }

    @Operation(summary = "获取任务审批历史")
    @GetMapping("/history/{instanceId}")
    public ResultVO<List<Map<String, Object>>> getHistory(@PathVariable Long instanceId) {
        List<Map<String, Object>> history = workflowTaskService.getTaskHistory(instanceId);
        return ResultVO.success(history);
    }

    @Operation(summary = "标记任务已读")
    @NoRepeatSubmit
    @PostMapping("/read/{taskId}")
    public ResultVO<Boolean> markAsRead(@PathVariable Long taskId) {
        return workflowTaskService.markTaskAsRead(taskId);
    }

    @Operation(summary = "获取待办任务数量")
    @GetMapping("/todo-count")
    public ResultVO<Long> getTodoCount() {
        Long count = workflowTaskService.getTodoCount(SecurityUtils.getUserId());
        return ResultVO.success(count);
    }

    @Operation(summary = "签收任务")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:task:claim')")
    @PostMapping("/claim/{taskId}")
    public ResultVO<Boolean> claim(@PathVariable Long taskId) {
        return workflowTaskService.claimTask(taskId);
    }

    @Operation(summary = "取消签收")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:task:unclaim')")
    @PostMapping("/unclaim/{taskId}")
    public ResultVO<Boolean> unclaim(@PathVariable Long taskId) {
        return workflowTaskService.unclaimTask(taskId);
    }
}
