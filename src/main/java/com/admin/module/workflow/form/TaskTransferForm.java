package com.admin.module.workflow.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务转办表单
 */
@Data
public class TaskTransferForm {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "转办人ID不能为空")
    private Long targetUserId;

    private String reason;
}
