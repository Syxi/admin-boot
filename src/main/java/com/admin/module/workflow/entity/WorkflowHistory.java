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
 * 流程历史记录实体
 * 用于记录流程操作历史
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "workflow_history")
public class WorkflowHistory extends BaseEntity {

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
     * 操作类型（1:提交 2:审批通过 3:审批驳回 4:转办 5:委派 6:撤回 7:终止 8:抄送 9:评论）
     */
    @TableField(value = "operation_type")
    private Integer operationType;

    /**
     * 操作人ID
     */
    @TableField(value = "operator_id")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @TableField(value = "operator_name")
    private String operatorName;

    /**
     * 操作人部门
     */
    @TableField(value = "operator_dept")
    private String operatorDept;

    /**
     * 操作意见
     */
    @TableField(value = "comment")
    private String comment;

    /**
     * 审批结果（1:通过 2:驳回）
     */
    @TableField(value = "result")
    private Integer result;

    /**
     * 表单数据JSON
     */
    @TableField(value = "form_data")
    private String formData;

    /**
     * 流程变量JSON
     */
    @TableField(value = "variables")
    private String variables;

    /**
     * 附件列表JSON
     */
    @TableField(value = "attachments")
    private String attachments;

    /**
     * 操作时间
     */
    @TableField(value = "operation_time")
    private LocalDateTime operationTime;

    /**
     * 持续时间（毫秒）
     */
    @TableField(value = "duration")
    private Long duration;

    /**
     * 目标节点ID（用于转办）
     */
    @TableField(value = "target_node_id")
    private String targetNodeId;

    /**
     * 目标节点名称
     */
    @TableField(value = "target_node_name")
    private String targetNodeName;

    /**
     * 目标处理人ID
     */
    @TableField(value = "target_assignee_id")
    private Long targetAssigneeId;

    /**
     * 目标处理人姓名
     */
    @TableField(value = "target_assignee_name")
    private String targetAssigneeName;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Long tenantId;

    /**
     * IP地址
     */
    @TableField(value = "ip_address")
    private String ipAddress;
}
