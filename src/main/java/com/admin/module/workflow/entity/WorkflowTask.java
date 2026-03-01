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
 * 流程任务实体
 * 用于管理流程任务信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "workflow_task")
public class WorkflowTask extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    /**
     * 流程实例ID
     */
    @TableField(value = "instance_id")
    private Long instanceId;

    /**
     * Flowable任务ID
     */
    @TableField(value = "task_id")
    private String taskId;

    /**
     * 流程定义ID
     */
    @TableField(value = "definition_id")
    private Long definitionId;

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
     * 任务类型（0:审批任务 1:抄送任务 2:办理任务）
     */
    @TableField(value = "task_type")
    private Integer taskType;

    /**
     * 处理人ID
     */
    @TableField(value = "assignee_id")
    private Long assigneeId;

    /**
     * 处理人姓名
     */
    @TableField(value = "assignee_name")
    private String assigneeName;

    /**
     * 候选人ID列表（逗号分隔）
     */
    @TableField(value = "candidate_ids")
    private String candidateIds;

    /**
     * 候选组
     */
    @TableField(value = "candidate_groups")
    private String candidateGroups;

    /**
     * 任务状态（0:待处理 1:已处理 2:已转办 3:已委派）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 审批结果（0:待审批 1:通过 2:驳回 3:转办 4:委派 5:撤回）
    */
    @TableField(value = "result")
    private Integer result;

    /**
     * 审批意见
     */
    @TableField(value = "comment")
    private String comment;

    /**
     * 表单数据JSON
     */
    @TableField(value = "form_data")
    private String formData;

    /**
     * 到达时间
     */
    @TableField(value = "arrive_time")
    private LocalDateTime arriveTime;

    /**
     * 处理时间
     */
    @TableField(value = "handle_time")
    private LocalDateTime handleTime;

    /**
     * 持续时间（毫秒）
     */
    @TableField(value = "duration")
    private Long duration;

    /**
     * 超时时间
     */
    @TableField(value = "due_time")
    private LocalDateTime dueTime;

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
     * 父任务ID（用于转办、委派）
     */
    @TableField(value = "parent_task_id")
    private Long parentTaskId;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Long tenantId;
}
