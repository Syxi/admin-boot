package com.admin.module.workflow.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程节点配置实体
 * 用于管理流程节点详细配置
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "workflow_node_config")
public class WorkflowNodeConfig extends BaseEntity {

    @TableId(value = "id")
    private Long id;

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
     * 节点类型（start:开始节点 userTask:用户任务 serviceTask:服务任务 exclusiveGateway:排他网关 parallelGateway:并行网关 end:结束节点）
     */
    @TableField(value = "node_type")
    private String nodeType;

    /**
     * 审批人类型（1:指定用户 2:指定角色 3:部门负责人 4:发起人自选 5:连续多级主管 6:发起人自己）
     */
    @TableField(value = "assignee_type")
    private Integer assigneeType;

    /**
     * 审批人IDs（逗号分隔）
     */
    @TableField(value = "assignee_ids")
    private String assigneeIds;

    /**
     * 审批角色IDs
     */
    @TableField(value = "role_ids")
    private String roleIds;

    /**
     * 部门级别（用于多级主管）
     */
    @TableField(value = "dept_level")
    private Integer deptLevel;

    /**
     * 多人审批方式（1:会签 2:或签 3:依次审批）
     */
    @TableField(value = "multi_approve_type")
    private Integer multiApproveType;

    /**
     * 审批通过比例（会签时有效）
     */
    @TableField(value = "pass_rate")
    private Integer passRate;

    /**
     * 是否允许转办（0:否 1:是）
     */
    @TableField(value = "allow_transfer")
    private Integer allowTransfer;

    /**
     * 是否允许委派（0:否 1:是）
     */
    @TableField(value = "allow_delegate")
    private Integer allowDelegate;

    /**
     * 是否允许加签（0:否 1:是）
     */
    @TableField(value = "allow_add_sign")
    private Integer allowAddSign;

    /**
     * 是否允许退回（0:否 1:是）
     */
    @TableField(value = "allow_back")
    private Integer allowBack;

    /**
     * 退回方式（1:退回上一步 2:退回发起人 3:自由选择）
     */
    @TableField(value = "back_type")
    private Integer backType;

    /**
     * 是否必填意见（0:否 1:是）
     */
    @TableField(value = "require_comment")
    private Integer requireComment;

    /**
     * 是否允许抄送（0:否 1:是）
     */
    @TableField(value = "allow_cc")
    private Integer allowCc;

    /**
     * 抄送人IDs
     */
    @TableField(value = "cc_user_ids")
    private String ccUserIds;

    /**
     * 超时提醒设置（小时，0表示不提醒）
     */
    @TableField(value = "timeout_hours")
    private Integer timeoutHours;

    /**
     * 表单权限配置JSON
     */
    @TableField(value = "form_permissions")
    private String formPermissions;

    /**
     * 按钮权限配置JSON
     */
    @TableField(value = "button_permissions")
    private String buttonPermissions;

    /**
     * 条件表达式
     */
    @TableField(value = "condition_expression")
    private String conditionExpression;

    /**
     * 排序号
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Long tenantId;
}
