package com.admin.module.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 流程实例VO
 */
@Data
public class WorkflowInstanceVO {

    private Long id;

    private Long definitionId;

    private String processName;

    private String processKey;

    private String processInstanceId;

    private String businessType;

    private String businessKey;

    private String businessTitle;

    private Long applicantId;

    private String applicantName;

    private String applicantAvatar;

    private Long deptId;

    private String deptName;

    private Integer status;

    private String statusName;

    private Integer result;

    private String resultName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long duration;

    private String durationText;

    private String currentNodeId;

    private String currentNodeName;

    private Long currentAssigneeId;

    private String currentAssigneeName;

    private String currentAssigneeAvatar;

    private Map<String, Object> formData;

    private Map<String, Object> variables;

    private Long tenantId;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private List<Map<String, Object>> historyList;

    private List<Map<String, Object>> taskList;
}
