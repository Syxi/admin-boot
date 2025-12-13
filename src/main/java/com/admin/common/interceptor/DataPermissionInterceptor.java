package com.admin.common.interceptor;

import com.admin.common.annotation.DataPermission;
import com.admin.common.context.DataPermissionContext;
import com.admin.common.enums.DataScopeEnum;
import com.admin.common.security.SecurityUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据权限拦截器
 * 通过拦截 SQL 执行，自动添加数据权限条件
 *
 * @author YourName
 * @since 2025-12-12
 */
@Slf4j
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class DataPermissionInterceptor implements InnerInterceptor {

    // 使用ThreadLocal存储当前方法的注解信息
    private static final ThreadLocal<DataPermission> ANNOTATION_HOLDER = new ThreadLocal<>();
    
    // SQL表名提取的正则表达式
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("\\b(from|join|update|into)\\s+([a-zA-Z_][a-zA-Z0-9_]*)", Pattern.CASE_INSENSITIVE);

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        try {
            MetaObject metaObject = MetaObject.forObject(sh, new DefaultObjectFactory(),
                    new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
            
            // 获取原始SQL
            BoundSql boundSql = sh.getBoundSql();
            String originalSql = boundSql.getSql();
            
            // 检查是否需要添加数据权限条件
            String newSql = processDataPermission(originalSql);
            
            // 如果SQL被修改，则更新SQL
            if (!originalSql.equals(newSql)) {
                metaObject.setValue("delegate.boundSql.sql", newSql);
            }
        } catch (Exception e) {
            log.warn("处理数据权限时发生异常: ", e);
        }
    }

    /**
     * 处理数据权限
     * @param originalSql 原始SQL
     * @return 处理后的SQL
     */
    private String processDataPermission(String originalSql) {
        try {
            // 检查是否有数据权限上下文
            Integer scope = SecurityUtils.getDataScope();
            if (scope == null || SecurityUtils.isAdmin() || scope.equals(DataScopeEnum.ALL.getValue())) {
                return originalSql; // 无需添加权限条件
            }
            
            // 获取注解信息
            DataPermission annotation = getCurrentAnnotation();
            String deptField = "dept_id";
            String userField = "create_user";
            
            if (annotation != null) {
                deptField = annotation.deptField();
                userField = annotation.userField();
            }
            
            // 根据不同的权限类型添加条件
            if (scope.equals(DataScopeEnum.CREATE_USER.getValue())) {
                // 本人数据权限 - 不受表名限制
                Long userId = DataPermissionContext.getUserId();
                if (userId != null) {
                    return addCreateUserCondition(originalSql, userId, userField);
                }
            } else {
                // 部门数据权限 - 需要检查表名
                String tableName = extractTableName(originalSql);
                if (!shouldAddDeptPermission(tableName)) {
                    return originalSql;
                }
                
                List<Long> deptIds = DataPermissionContext.getDeptIds();
                if (!CollectionUtils.isEmpty(deptIds)) {
                    return addDeptCondition(originalSql, deptIds, deptField, tableName);
                }
            }
        } catch (Exception e) {
            log.warn("处理数据权限时发生异常: ", e);
        }
        
        return originalSql;
    }

    /**
     * 从SQL中提取表名
     * @param sql SQL语句
     * @return 表名
     */
    private String extractTableName(String sql) {
        if (sql == null || sql.isEmpty()) {
            return null;
        }
        
        Matcher matcher = TABLE_NAME_PATTERN.matcher(sql);
        if (matcher.find()) {
            return matcher.group(2).toLowerCase();
        }
        
        return null;
    }

    /**
     * 判断是否需要添加部门权限
     * @param tableName 表名
     * @return 是否需要添加部门权限
     */
    private boolean shouldAddDeptPermission(String tableName) {
        if (tableName == null) {
            return false;
        }
        
        // 只有特定的表才需要添加部门权限
        switch (tableName) {
            case "sys_user":
            case "sys_menu":
            case "sys_dict_data":
            case "sys_config":
            case "sys_article":
                return true;
            default:
                return false;
        }
    }

    /**
     * 添加创建者条件
     * @param originalSql 原始SQL
     * @param userId 用户ID
     * @param userField 用户字段名
     * @return 添加条件后的SQL
     */
    private String addCreateUserCondition(String originalSql, Long userId, String userField) {
        // 检查SQL是否已经包含了用户字段的条件
        if (containsFieldCondition(originalSql, userField)) {
            return originalSql; // 已经有相关条件，不需要添加
        }
        
        // 提取表名以确定正确的用户字段
        String tableName = extractTableName(originalSql);
        String actualUserField = getUserFieldForTable(tableName, userField);
        
        // 使用参数化查询避免SQL注入
        String condition = " " + actualUserField + " = " + userId;
        return addWhereCondition(originalSql, condition);
    }

    /**
     * 根据表名获取正确的用户字段名
     * @param tableName 表名
     * @param defaultUserField 默认用户字段名
     * @return 正确的用户字段名
     */
    private String getUserFieldForTable(String tableName, String defaultUserField) {
        if (tableName == null) {
            return defaultUserField;
        }
        
        // 对于关联表，使用user_id字段而不是create_user字段
        switch (tableName) {
            case "sys_user_role":
            case "sys_user_post":
            case "sys_user_dept":
                return "user_id";
            default:
                return defaultUserField;
        }
    }

    /**
     * 添加部门条件
     * @param originalSql 原始SQL
     * @param deptIds 部门ID列表
     * @param deptField 部门字段名
     * @param tableName 表名
     * @return 添加条件后的SQL
     */
    private String addDeptCondition(String originalSql, List<Long> deptIds, String deptField, String tableName) {
        // 检查SQL是否已经包含了部门字段的条件
        if (containsFieldCondition(originalSql, deptField)) {
            return originalSql; // 已经有相关条件，不需要添加
        }
        
        // 特殊处理sys_dept表，使用id字段而不是dept_id字段
        if ("sys_dept".equals(tableName) && "dept_id".equals(deptField)) {
            deptField = "id";
        }
        
        StringBuilder deptCondition = new StringBuilder();
        if (deptIds.size() == 1) {
            deptCondition.append(" ").append(deptField).append(" = ").append(deptIds.get(0));
        } else {
            deptCondition.append(" ").append(deptField).append(" IN (");
            for (int i = 0; i < deptIds.size(); i++) {
                if (i > 0) deptCondition.append(",");
                deptCondition.append(deptIds.get(i));
            }
            deptCondition.append(")");
        }
        return addWhereCondition(originalSql, deptCondition.toString());
    }

    /**
     * 检查SQL是否已经包含了指定字段的条件
     * @param sql SQL语句
     * @param fieldName 字段名
     * @return 是否包含字段条件
     */
    private boolean containsFieldCondition(String sql, String fieldName) {
        if (sql == null || fieldName == null) {
            return false;
        }
        
        // 使用正则表达式更精确地检查字段条件
        String trimmedSql = sql.trim();
        String trimmedFieldName = fieldName.trim();
        
        if (trimmedSql.isEmpty() || trimmedFieldName.isEmpty()) {
            return false;
        }
        
        // 检查常见的SQL条件格式
        String[] patterns = {
            "\\b" + Pattern.quote(trimmedFieldName) + "\\s*=\\s*\\d+",           // field = value
            "\\b" + Pattern.quote(trimmedFieldName) + "\\s*in\\s*\\([^)]*\\)",   // field IN (...)
            "\\b" + Pattern.quote(trimmedFieldName) + "\\s*>\\s*\\d+",           // field > value
            "\\b" + Pattern.quote(trimmedFieldName) + "\\s*<\\s*\\d+",           // field < value
            "\\b" + Pattern.quote(trimmedFieldName) + "\\s*>=\\s*\\d+",          // field >= value
            "\\b" + Pattern.quote(trimmedFieldName) + "\\s*<=\\s*\\d+"           // field <= value
        };
        
        String lowerSql = trimmedSql.toLowerCase();
        
        for (String pattern : patterns) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(lowerSql).find()) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 在SQL中添加WHERE条件
     * @param originalSql 原始SQL
     * @param condition 条件
     * @return 添加条件后的SQL
     */
    private String addWhereCondition(String originalSql, String condition) {
        if (originalSql == null || condition == null) {
            return originalSql;
        }
        
        String trimmedSql = originalSql.trim();
        if (trimmedSql.isEmpty()) {
            return originalSql;
        }
        
        String lowerSql = trimmedSql.toLowerCase();
        
        // 如果已经有WHERE子句
        if (lowerSql.contains(" where ")) {
            // 查找WHERE子句后的位置
            int whereIndex = lowerSql.indexOf(" where ") + " where ".length();
            
            // 检查是否有ORDER BY子句
            int orderByIndex = lowerSql.indexOf(" order by ");
            // 检查是否有LIMIT子句
            int limitIndex = lowerSql.indexOf(" limit ");
            
            // 确定插入位置
            int insertIndex = trimmedSql.length(); // 默认在末尾
            
            // 如果有ORDER BY，就在ORDER BY前插入
            if (orderByIndex > whereIndex) {
                insertIndex = orderByIndex;
            } 
            // 如果有LIMIT且没有ORDER BY，就在LIMIT前插入
            else if (limitIndex > whereIndex) {
                insertIndex = limitIndex;
            }
            
            return trimmedSql.substring(0, insertIndex) + " AND " + condition + trimmedSql.substring(insertIndex);
        } 
        // 如果有GROUP BY子句
        else if (lowerSql.contains(" group by ")) {
            int index = lowerSql.indexOf(" group by ");
            return trimmedSql.substring(0, index) + " WHERE " + condition + trimmedSql.substring(index);
        }
        // 如果有ORDER BY子句
        else if (lowerSql.contains(" order by ")) {
            int index = lowerSql.indexOf(" order by ");
            return trimmedSql.substring(0, index) + " WHERE " + condition + trimmedSql.substring(index);
        }
        // 如果有LIMIT子句
        else if (lowerSql.contains(" limit ")) {
            int index = lowerSql.indexOf(" limit ");
            return trimmedSql.substring(0, index) + " WHERE " + condition + trimmedSql.substring(index);
        }
        // 其他情况直接添加WHERE子句
        else {
            return trimmedSql + " WHERE " + condition;
        }
    }

    /**
     * 设置当前方法的注解信息
     * @param annotation 注解
     */
    public static void setCurrentAnnotation(DataPermission annotation) {
        ANNOTATION_HOLDER.set(annotation);
    }

    /**
     * 清除当前方法的注解信息
     */
    public static void clearCurrentAnnotation() {
        ANNOTATION_HOLDER.remove();
    }

    /**
     * 获取当前方法的注解信息
     * @return 注解
     */
    public static DataPermission getCurrentAnnotation() {
        return ANNOTATION_HOLDER.get();
    }
}