package com.admin.module.workflow.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务委派表单
 */
@Data
public class TaskDelegateForm {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "委派人ID不能为空")
    private Long delegateUserId;

    private String reason;
}
