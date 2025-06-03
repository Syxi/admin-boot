package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @TableName sys_organization
 */
@TableName(value ="sys_organization")
@Data
public class SysOrganization implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 组织名称
     */
    private String organName;

    /**
     * 组织编码
     */
    private String organCode;

    /**
     * 组织类型 (1：机构，2：部门）
     */
    private Integer organType;

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
     * 组织图标
     */
    private String organImg;

    /**
     * 组织简介
     */
    private String organIntroduction;

    /**
     * 组织联系电话
     */
    private String organPhone;

    /**
     * 组织地址
     */
    private String organAddress;

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