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
        if (tenantId == null) {
            throw new RuntimeException("无法获取当前租户ID，请检查请求头或认证信息");
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id"; // 数据库字段名
    }

    // 忽略租户过滤的表（如租户表、字典表）
    @Override
    public boolean ignoreTable(String tableName) {
        return "sys_tenant".equalsIgnoreCase(tableName) ||
                "sys_dict".equalsIgnoreCase(tableName);
    }
}
