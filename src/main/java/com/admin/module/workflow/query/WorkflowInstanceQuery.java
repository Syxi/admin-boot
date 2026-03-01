package com.admin.module.workflow.query;

import com.admin.common.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程实例查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkflowInstanceQuery extends BasePage {

    private Long definitionId;

    private Long applicantId;

    private Integer status;

    private Integer result;

    private String businessType;

    private String businessTitle;

    private LocalDateTime startTimeBegin;

    private LocalDateTime startTimeEnd;
}
