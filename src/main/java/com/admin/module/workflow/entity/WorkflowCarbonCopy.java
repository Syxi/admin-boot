package com.admin.module.workflow.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流程抄送记录实体
 * 用于管理流程抄送信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "workflow_carbon_copy")
public class WorkflowCarbonCopy extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    /**
     * 流程实例ID
     */
    @TableField(value = "instance_id")
    private Long instanceId;

    /**
     * 任务ID
     */
    @TableField(value = "task_id")
    private Long taskId;

    /**
     * 节点ID
     */
    @TableField(value = "node_id")
    private String nodeId;

    /**
     * 节点名称
     */
    @TableField(value = "node_name")
    private String nodeName;

    /**
     * 抄送人ID
     */
    @TableField(value = "cc_user_id")
    private Long ccUserId;

    /**
     * 抄送人姓名
     */
    @TableField(value = "cc_user_name")
    private String ccUserName;

    /**
     * 是否已读（0:未读 1:已读）
     */
    @TableField(value = "is_read")
    private Integer isRead;

    /**
     * 阅读时间
     */
    @TableField(value = "read_time")
    private LocalDateTime readTime;

    /**
     * 抄送时间
     */
    @TableField(value = "cc_time")
    private LocalDateTime ccTime;

    /**
     * 抄送原因/备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Long tenantId;
}
