package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户配置表（关联租户和套餐）
 * @TableName sys_tenant_config
 */
@TableName(value ="sys_tenant_config")
@Data
public class SysTenantConfig extends BaseEntity {
    /**
     * 主键
     */
    @TableId()
    private Long id;


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


}