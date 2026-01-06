package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户配置表（关联租户和套餐）
 * @TableName sys_tenant_config
 */
@TableName(value ="sys_tenant_config")
@Data
public class SysTenantConfig {
    /**
     * 主键
     */
    @TableId()
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 套餐ID
     */
    private Long packageId;

    /**
     * 套餐开始时间
     */
    private LocalDateTime startTime;

    /**
     * 套餐结束时间
     */
    private LocalDateTime endTime;

    /**
     * 当前用户数
     */
    private Integer currentUsers;

    /**
     * 当前存储使用量(MB)
     */
    private Long currentStorage;

    /**
     * 状态: 1-正常, -1-过期, -2-禁用
     */
    private Integer status;

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