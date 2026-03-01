package com.admin.module.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程任务VO
 */
@Data
public class WorkflowTaskVO {

    private Long id;

    private Long instanceId;

    private String processName;

    private String businessTitle;

    private String businessType;

    private String taskId;

    private Long definitionId;

    private String nodeId;

    private String nodeName;

    private Integer taskType;

    private String taskTypeName;

    private Long assigneeId;

    private String assigneeName;

    private String assigneeAvatar;

    private String candidateIds;

    private String candidateGroups;

    private Integer status;

    private String statusName;

    private Integer result;

    private String resultName;

    private String comment;

    private Map<String, Object> formData;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime arriveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    private Long duration;

    private String durationText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueTime;

    private Integer isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    private Long parentTaskId;

    private Long tenantId;

    private Long applicantId;

    private String applicantName;

    private String applicantAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Map<String, Object> formPermissions;

    private Map<String, Object> buttonPermissions;
}
