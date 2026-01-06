package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 租户套餐菜单权限关联表
 * @TableName sys_tenant_package_menu
 */
@TableName(value ="sys_tenant_package_menu")
@Data
public class SysTenantPackageMenu {
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
     * 菜单ID
     */
    private Long menuId;

    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long createUser;

    /**
     * 逻辑删除标识(0:未删除;-1:已删除)
     */
    private Integer deleted;
}