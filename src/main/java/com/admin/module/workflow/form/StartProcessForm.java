package com.admin.module.workflow.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 启动流程表单
 */
@Data
public class StartProcessForm {

    @NotNull(message = "流程定义ID不能为空")
    private Long definitionId;

    private String businessType;

    private String businessKey;

    private String businessTitle;

    private Map<String, Object> formData;

    private Map<String, Object> variables;

    private String remark;

    private Long ccUserId;
}
