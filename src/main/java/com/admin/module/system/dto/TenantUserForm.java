package com.admin.module.system.dto;

import lombok.Data;

/**
 * 租户用户关联表单对象
 * 
 * @author suYan
 * @date 2025/11/20 10:35
 */
@Data
public class TenantUserForm {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 租户名称
     */
    private String tenantName;
}