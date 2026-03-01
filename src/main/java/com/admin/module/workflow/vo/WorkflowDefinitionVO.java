package com.admin.module.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程定义VO
 */
@Data
public class WorkflowDefinitionVO {

    private Long id;

    private String processKey;

    private String processName;

    private String category;

    private String description;

    private Integer version;

    private Integer status;

    private String statusName;

    private Integer formType;

    private Long formId;

    private String formName;

    private Long tenantId;

    private Integer isMultiInstance;

    private Integer timeoutRemind;

    private String icon;

    private Integer sortOrder;

    private String flowableDefinitionId;

    private String deploymentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Long createBy;

    private String createByName;
}
