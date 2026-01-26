package com.admin.common.handler;

import com.admin.common.security.SecurityUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

/**
 * @author suYan
 * @date 2025/11/18 17:04
 * 多租户插件（自动过滤）
 */
@Slf4j
@Component
public class MyTenantLineHandler implements TenantLineHandler {

    @Override
    public Expression getTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        
        // 如果是超级管理员，允许查看所有数据
        boolean isAdmin = SecurityUtils.isAdmin();
        if (isAdmin) {
            log.debug("当前用户是超级管理员，允许查看所有数据");
            return null; // 跳过多租户过滤
        }
        
        // 对于普通用户，如果没有租户ID，需要特别处理
        if (tenantId == null || tenantId == 0) {
            // 返回0，这样普通用户将只能看到租户ID为0的数据（通常没有）
            // 或者我们可以抛出异常，要求用户必须有租户ID
            return new LongValue(0L); // 返回0表示没有租户数据可访问
        }

        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id"; // 数据库字段名
    }

    // 忽略租户过滤的表（如租户表、用户表等系统表，以及字典表等公共表）
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
