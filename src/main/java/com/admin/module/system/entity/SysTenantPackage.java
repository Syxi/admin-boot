package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户套餐表
 * @TableName sys_tenant_package
 */
@TableName(value ="sys_tenant_package")
@Data
public class SysTenantPackage {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐编码
     */
    private String code;

    /**
     * 套餐描述
     */
    private String description;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大存储空间(MB)
     */
    private Long maxStorage;

    /**
     * 有效期(天)
     */
    private Integer validityDays;

    /**
     * 1:启用 -1:禁用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

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
     * 逻辑删除标识(0:未删除;-1:已删除)
     */
    private Integer deleted;
}