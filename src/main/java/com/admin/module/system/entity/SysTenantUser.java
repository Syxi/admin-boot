package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Long tenantId;

    public SysTenantUser(Long userId, Long tenantId) {
        this.userId = userId;
        this.tenantId = tenantId;
    }
}