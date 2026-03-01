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
 * 流程定义实体
 * 用于管理自定义流程定义信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "workflow_definition")
public class WorkflowDefinition extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    /**
     * 流程定义Key
     */
    @TableField(value = "process_key")
    private String processKey;

    /**
     * 流程名称
     */
    @TableField(value = "process_name")
    private String processName;

    /**
     * 流程分类
     */
    @TableField(value = "category")
    private String category;

    /**
     * 流程描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 流程模型XML内容
     */
    @TableField(value = "model_xml")
    private String modelXml;

    /**
     * 流程图SVG
     */
    @TableField(value = "diagram_svg")
    private String diagramSvg;

    /**
     * 流程版本
     */
    @TableField(value = "version")
    private Integer version;

    /**
     * 状态（0:草稿 1:已发布 2:已停用）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 表单类型（0:自定义表单 1:在线表单）
     */
    @TableField(value = "form_type")
    private Integer formType;

    /**
     * 表单ID
     */
    @TableField(value = "form_id")
    private Long formId;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Long tenantId;

    /**
     * 是否多实例（0:否 1:是）
     */
    @TableField(value = "is_multi_instance")
    private Integer isMultiInstance;

    /**
     * 超时提醒设置（小时）
     */
    @TableField(value = "timeout_remind")
    private Integer timeoutRemind;

    /**
     * 流程图标
     */
    @TableField(value = "icon")
    private String icon;

    /**
     * 排序号
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    /**
     * Flowable流程定义ID
     */
    @TableField(value = "flowable_definition_id")
    private String flowableDefinitionId;

    /**
     * 部署ID
     */
    @TableField(value = "deployment_id")
    private String deploymentId;
}
