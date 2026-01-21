package com.admin.common.handler;

import com.admin.common.security.SecurityUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

/**
 * @author suYan
 * @date 2025/11/18 17:04
 * 多租户插件（自动过滤）
 */
@Component
public class MyTenantLineHandler implements TenantLineHandler {

    @Override
    public Expression getTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        
        // 如果是超级管理员，允许查看所有数据
        if (com.admin.common.security.SecurityUtils.isAdmin()) {
            return null; // 跳过多租户过滤
        }
        
        // 如果无法获取租户ID或租户ID为0，返回null以跳过多租户过滤
        // 这样可以避免用户看不到数据的问题，但需要确保业务数据正确分配了租户ID
        if (tenantId == null || tenantId == 0) {
            return null;
        }
        
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id"; // 数据库字段名
    }

    // 忽略租户过滤的表（系统级表，供所有租户共享使用）
    @Override
    public boolean ignoreTable(String tableName) {
        return "sys_tenant".equalsIgnoreCase(tableName) ||
                "sys_dict_type".equalsIgnoreCase(tableName) ||
                "sys_dict_value".equalsIgnoreCase(tableName) ||
                "sys_tenant_user".equalsIgnoreCase(tableName);
    }
}
