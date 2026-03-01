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
 * 流程表单实体
 * 用于管理自定义表单
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "workflow_form")
public class WorkflowForm extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    /**
     * 表单名称
     */
    @TableField(value = "form_name")
    private String formName;

    /**
     * 表单编码
     */
    @TableField(value = "form_code")
    private String formCode;

    /**
     * 表单分类
     */
    @TableField(value = "category")
    private String category;

    /**
     * 表单描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 表单JSON配置
     */
    @TableField(value = "form_config")
    private String formConfig;

    /**
     * 表单字段配置JSON
     */
    @TableField(value = "field_config")
    private String fieldConfig;

    /**
     * 状态（0:草稿 1:已发布 2:已停用）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 版本号
     */
    @TableField(value = "version")
    private Integer version;

    /**
     * 是否默认表单（0:否 1:是）
     */
    @TableField(value = "is_default")
    private Integer isDefault;

    /**
     * 图标
     */
    @TableField(value = "icon")
    private String icon;

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
