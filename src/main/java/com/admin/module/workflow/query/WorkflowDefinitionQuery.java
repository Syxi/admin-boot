package com.admin.module.workflow.query;

import com.admin.common.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程定义查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkflowDefinitionQuery extends BasePage {

    private String processKey;

    private String processName;

    private String category;

    private Integer status;
}
