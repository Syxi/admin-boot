package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @TableName sys_dept
 */
@TableName(value ="sys_dept")
@Data
public class SysDept implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 部门类型 (1：机构，2：部门）
     */
    private Integer deptType;

    /**
     * 父节点id
     */
    private Long parentId;

    /**
     * 父节点路径
     */
    private String treePath;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 (1: 启动，-1：禁用)
     */
    private Integer status;

    /**
     * 部门图标
     */
    private String deptImg;

    /**
     * 部门简介
     */
    private String deptIntroduction;

    /**
     * 部门联系电话
     */
    private String deptPhone;

    /**
     * 部门地址
     */
    private String deptAddress;

    /**
     * 备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long updateUser;

    /**
     * 逻辑删除标识(0:未删除，-1:已删除)
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}