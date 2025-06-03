package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位表
 * @TableName sys_position
 */
@TableName(value ="sys_position")
@Data
public class SysPosition implements Serializable {
    /**
     * 
     */
    @TableId
    private Long positionId;

    /**
     * 岗位名称
     */
    private String positionName;


    /**
     * 岗位描述
     */
    private String description;

    /**
     * 状态 (1: 正常，-1：禁用)
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 薪资范围
     */
    private String salaryRange;

    /**
     * 工作经验
     */
    private String experience;

    /**
     * 教育背景 (0: 无，1: 专科，2：本科，3: 研究生，4: 博士，5：硕士)
     */
    private Integer education;

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
     * 逻辑删除标识(0-未删除；1-已删除)
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}