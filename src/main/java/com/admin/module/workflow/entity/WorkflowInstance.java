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
 * 流程实例实体
 * 用于管理流程实例信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "workflow_instance")
public class WorkflowInstance extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    /**
     * 流程定义ID
     */
    @TableField(value = "definition_id")
    private Long definitionId;

    /**
     * Flowable流程实例ID
     */
    @TableField(value = "process_instance_id")
    private String processInstanceId;

    /**
     * 业务类型
     */
    @TableField(value = "business_type")
    private String businessType;

    /**
     * 业务Key
     */
    @TableField(value = "business_key")
    private String businessKey;

    /**
     * 业务标题
     */
    @TableField(value = "business_title")
    private String businessTitle;

    /**
     * 申请人ID
     */
    @TableField(value = "applicant_id")
    private Long applicantId;

    /**
     * 申请人姓名
     */
    @TableField(value = "applicant_name")
    private String applicantName;

    /**
     * 申请人部门ID
     */
    @TableField(value = "dept_id")
    private Long deptId;

    /**
     * 申请人部门名称
     */
    @TableField(value = "dept_name")
    private String deptName;

    /**
     * 流程状态（0:运行中 1:已完成 2:已终止 3:已挂起）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 流程结果（0:审批中 1:通过 2:驳回 3:撤回）
     */
    @TableField(value = "result")
    private Integer result;

    /**
     * 开始时间
     */
    @TableField(value = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField(value = "end_time")
    private LocalDateTime endTime;

    /**
     * 持续时间（毫秒）
     */
    @TableField(value = "duration")
    private Long duration;

    /**
     * 当前节点ID
     */
    @TableField(value = "current_node_id")
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    @TableField(value = "current_node_name")
    private String currentNodeName;

    /**
     * 当前处理人ID
     */
    @TableField(value = "current_assignee_id")
    private Long currentAssigneeId;

    /**
     * 当前处理人姓名
     */
    @TableField(value = "current_assignee_name")
    private String currentAssigneeName;

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
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Long tenantId;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}
