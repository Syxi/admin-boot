package com.admin.module.workflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程抄送VO
 */
@Data
public class WorkflowCarbonCopyVO {

    private Long id;

    private Long instanceId;

    private String processName;

    private String businessTitle;

    private String businessType;

    private Long taskId;

    private String nodeId;

    private String nodeName;

    private Long ccUserId;

    private String ccUserName;

    private String ccUserAvatar;

    private Integer isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ccTime;

    private String remark;

    private Long tenantId;

    private Long applicantId;

    private String applicantName;

    private String applicantAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
