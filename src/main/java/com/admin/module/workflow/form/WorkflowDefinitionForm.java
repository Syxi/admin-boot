package com.admin.module.workflow.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 流程定义表单
 */
@Data
public class WorkflowDefinitionForm {

    private Long id;

    @NotBlank(message = "流程Key不能为空")
    private String processKey;

    @NotBlank(message = "流程名称不能为空")
    private String processName;

    private String category;

    private String description;

    private String modelXml;

    private String diagramSvg;

    private Integer formType;

    private Long formId;

    private Integer isMultiInstance;

    private Integer timeoutRemind;

    private String icon;

    private Integer sortOrder;
}
