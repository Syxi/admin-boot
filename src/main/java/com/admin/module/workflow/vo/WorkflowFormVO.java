package com.admin.module.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 流程表单VO
 */
@Data
public class WorkflowFormVO {

    private Long id;

    private String formName;

    private String formCode;

    private String category;

    private String description;

    private Map<String, Object> formConfig;

    private List<Map<String, Object>> fieldConfig;

    private Integer status;

    private String statusName;

    private Integer version;

    private Integer isDefault;

    private String icon;

    private Integer sortOrder;

    private Long tenantId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Long createBy;

    private String createByName;
}
