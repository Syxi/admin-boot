package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.common.security.SecurityUtils;
import com.admin.module.workflow.service.WorkflowCarbonCopyService;
import com.admin.module.workflow.vo.WorkflowCarbonCopyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程抄送控制器
 */
@Slf4j
@Tag(name = "流程抄送接口")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/carbon-copy")
public class WorkflowCarbonCopyController {

    private final WorkflowCarbonCopyService workflowCarbonCopyService;

    @Operation(summary = "抄送记录分页列表")
    @GetMapping("/page")
    public PageResult<WorkflowCarbonCopyVO> page(
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return workflowCarbonCopyService.selectCcPage(SecurityUtils.getUserId(), isRead, page, limit);
    }

    @Operation(summary = "标记已读")
    @NoRepeatSubmit
    @PostMapping("/read/{id}")
    public ResultVO<Boolean> markAsRead(@PathVariable Long id) {
        return workflowCarbonCopyService.markAsRead(id);
    }

    @Operation(summary = "全部标记已读")
    @NoRepeatSubmit
    @PostMapping("/read-all")
    public ResultVO<Boolean> markAllAsRead() {
        return workflowCarbonCopyService.markAllAsRead(SecurityUtils.getUserId());
    }

    @Operation(summary = "获取未读抄送数量")
    @GetMapping("/unread-count")
    public ResultVO<Long> getUnreadCount() {
        Long count = workflowCarbonCopyService.getUnreadCount(SecurityUtils.getUserId());
        return ResultVO.success(count);
    }

    @Operation(summary = "获取流程的抄送记录")
    @GetMapping("/instance/{instanceId}")
    public ResultVO<List<WorkflowCarbonCopyVO>> getByInstanceId(@PathVariable Long instanceId) {
        List<WorkflowCarbonCopyVO> list = workflowCarbonCopyService.getCcByInstanceId(instanceId);
        return ResultVO.success(list);
    }
}
