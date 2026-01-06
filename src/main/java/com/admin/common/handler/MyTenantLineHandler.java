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
        // 如果无法获取租户ID，返回null，这样MyBatis-Plus会跳过多租户过滤
        if (tenantId == null || tenantId == 0) {
            return null;
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id"; // 数据库字段名
    }

    // 忽略租户过滤的表（如租户表、字典表等系统表）
    @Override
    public boolean ignoreTable(String tableName) {
        return "sys_tenant".equalsIgnoreCase(tableName) ||
                "sys_user".equalsIgnoreCase(tableName) ||
                "sys_user_role".equalsIgnoreCase(tableName) ||
                "sys_role".equalsIgnoreCase(tableName) ||
                "sys_role_menu".equalsIgnoreCase(tableName) ||
                "sys_menu".equalsIgnoreCase(tableName) ||
                "sys_dept".equalsIgnoreCase(tableName) ||
                "sys_position".equalsIgnoreCase(tableName) ||
                "sys_dict_type".equalsIgnoreCase(tableName) ||
                "sys_dict_value".equalsIgnoreCase(tableName) ||
                "sys_tenant_user".equalsIgnoreCase(tableName);
    }
}
