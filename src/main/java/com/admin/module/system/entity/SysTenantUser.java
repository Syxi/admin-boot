package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 租户用户关联表
 * @TableName sys_tenant_user
 */
@TableName(value ="sys_tenant_user")
@Data
public class SysTenantUser {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 租户id
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long tenantId;

    public SysTenantUser(Long userId, Long tenantId) {
        this.userId = userId;
        this.tenantId = tenantId;
    }
}