package com.admin.module.workflow.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 流程表单Form
 */
@Data
public class WorkflowFormForm {

    private Long id;

    @NotBlank(message = "表单名称不能为空")
    private String formName;

    @NotBlank(message = "表单编码不能为空")
    private String formCode;

    private String category;

    private String description;

    private String formConfig;

    private String fieldConfig;

    private String icon;

    private Integer sortOrder;
}
