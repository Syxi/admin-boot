package com.admin.common.handler;

import com.admin.common.annotation.DataPermission;
import com.admin.common.enums.DataScopeEnum;
import com.admin.common.enums.IBaseEnum;
import com.admin.common.security.SecurityUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * 数据权限控制器
 */
@Slf4j
public class MyDataPermissionHandler implements DataPermissionHandler {


    /**
     * 获取数据权限的sql片段
     * @param where 查询条件
     * @param mappedStatementId mapper接口方法的全路径
     * @return
     */
    @SneakyThrows
    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        // 如果是未登录，或者是定时任务执行的sql，或者是超级管理员，执行返回
        if (SecurityUtils.getUserId() == null || SecurityUtils.isAdmin()) {
            return where;
        }

        // 获取当前用户的数据权限
        Integer dataScope = SecurityUtils.getDataScope();
        if (dataScope == null) {
            return where;
        }
        DataScopeEnum dataScopeEnum = IBaseEnum.getEnumByValue(dataScope, DataScopeEnum.class);
        // 如果是全部数据权限，直接返回
        if (DataScopeEnum.ALL == dataScopeEnum) {
            return where;
        }

        // 获取当前执行的借口类
        Class<?> clazz = Class.forName(mappedStatementId.substring(0, mappedStatementId.lastIndexOf(StringPool.DOT)));
        // 获取当前执行的方法名称
        String methodName = mappedStatementId.substring(mappedStatementId.lastIndexOf(StringPool.DOT) + 1);
        // 获取当前执行的接口类里所有的方法
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                DataPermission dataPermissionAnnotation = method.getAnnotation(DataPermission.class);
                // 判断当前执行的方法是否有权限注解，如果没有注解直接返回
                if (dataPermissionAnnotation == null) {
                    return where;
                }

                return dataScopeFilter(dataPermissionAnnotation.orgAlias(), dataPermissionAnnotation.orgIdColumnName(),
                        dataPermissionAnnotation.userAlias(), dataPermissionAnnotation.userIdColumnName(), dataScopeEnum, where);
            }
        }


        return where;
    }


    @SneakyThrows
    public static Expression dataScopeFilter(String orgAlias, String orgIdColumName, String userAlias,
                                             String userIdColumName, DataScopeEnum dataScopeEnum, Expression where) {
        // 获取组织和用户的别名
        String orgColumnName = StringUtils.isNotBlank(orgAlias) ? (orgAlias + StringPool.DOT + orgIdColumName) : orgIdColumName;
        String userColumnName  = StringUtils.isNotBlank(orgAlias) ? (userAlias + StringPool.DOT + userIdColumName) : userIdColumName;

        Long orgId;
        Long userId;
        String appendSqlStr;
        switch (dataScopeEnum) {
            case ALL:
                return where;
            case ORGANIZATION:
                orgId = SecurityUtils.getDeptId();
                appendSqlStr = orgColumnName + StringPool.EQUALS + orgId;
                break;
            case CREATE_USER:
                userId = SecurityUtils.getUserId();
                appendSqlStr = userColumnName + StringPool.EQUALS + userId;
                break;
            default:
                orgId = SecurityUtils.getDeptId();
                appendSqlStr = orgColumnName + " IN ( SELECT id FROM sys_organization where id = " + orgId + " OR FIND_IN_SET(" + orgId + ", tree_path) )";
                break;
        }

        if (StringUtils.isBlank(appendSqlStr)) {
            return where;
        }

        Expression appendExpression = CCJSqlParserUtil.parseCondExpression(appendSqlStr);

        if (where == null) {
            return appendExpression;
        }

        return new AndExpression(where, appendExpression);
    }

}
