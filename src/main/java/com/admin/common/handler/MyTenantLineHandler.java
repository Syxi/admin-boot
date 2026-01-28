package com.admin.common.handler;

import com.admin.common.constant.SystemConstants;
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
        
        // 如果无法获取租户ID（如系统启动时或未认证用户），返回默认值0，避免影响非多租户场景
        if (tenantId == null) {
            return new LongValue(0L);
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
        // 如果是超级管理员，跳过多租户过滤
        String username = SecurityUtils.getUserName();
        if (SystemConstants.ADMIN_USERNAME.equalsIgnoreCase(username)) {
            return true;
        }

        return "sys_tenant".equalsIgnoreCase(tableName) ||
                "sys_user".equalsIgnoreCase(tableName) ||
                "sys_user_role".equalsIgnoreCase(tableName) ||
                "sys_role".equalsIgnoreCase(tableName) ||
                "sys_role_menu".equalsIgnoreCase(tableName) ||
                "sys_menu".equalsIgnoreCase(tableName) ||
                "sys_dept".equalsIgnoreCase(tableName) ||
                "sys_dict_type".equalsIgnoreCase(tableName) ||
                "sys_dict_value".equalsIgnoreCase(tableName) ||
                "sys_tenant_user".equalsIgnoreCase(tableName) ||
                "scheduled_job".equalsIgnoreCase(tableName);
    }
}
