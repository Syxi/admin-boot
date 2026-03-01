package com.admin.module.workflow.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 任务完成表单
 */
@Data
public class TaskCompleteForm {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    private String comment;

    private Map<String, Object> formData;

    private Map<String, Object> variables;

    private String nextNodeId;

    private Long nextAssigneeId;

    private Boolean pass;
}
