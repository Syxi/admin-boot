package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台系统表
 * @TableName t_platform
 */
@TableName(value ="t_platform")
@Data
public class Platform {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 平台名称
     */
    private String name;

    /**
     * 平台路由路径
     */
    private String path;

    /**
     * 平台图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 1启用，-1禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remake;

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
}