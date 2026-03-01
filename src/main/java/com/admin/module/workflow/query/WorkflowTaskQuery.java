package com.admin.module.workflow.query;

import com.admin.common.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程任务查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkflowTaskQuery extends BasePage {

    private Long assigneeId;

    private String processName;

    private String businessTitle;

    private Integer status;

    private Integer taskType;
}
