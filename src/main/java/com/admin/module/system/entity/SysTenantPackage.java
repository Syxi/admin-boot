package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
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
public class SysTenantPackage extends BaseEntity {
    /**
     * 主键
     */
    @TableId()
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


}