package com.admin.common.handler;

import com.admin.common.enums.DataScopeEnum;
import com.admin.common.security.SecurityUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.stereotype.Component;

/**
 * 自定义数据权限处理器
 * <p>
 * 作用：根据当前用户的数据权限范围（如“本部门”、“本部门及子部门”），
 * 动态向 SQL 查询中注入 WHERE 条件，实现行级数据过滤。
 *
 * 用户数据权限处理器（基于中间表 sys_user_dept）
 * <p>
 * 适用场景：sys_user 表没有 dept_id 字段，通过中间表关联部门
 * </p>
 *
 *
 * @author YourName
 * @since 2025-12-11
 */
@Component
public class MyDataPermissionHandler implements DataPermissionHandler {

    private static final String[] TARGET_MAPPERS = {
            "com.yourcompany.mapper.SysUserMapper"
    };

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        if (!isTargetMapper(mappedStatementId)) {
            return null;
        }

        Long userId = SecurityUtils.getUserId();
        Long deptId = SecurityUtils.getDeptId();
        Integer dataScopeValue = SecurityUtils.getDataScope();

        if (userId == null || SecurityUtils.isAdmin() || dataScopeValue == null) {
            return null;
        }

        DataScopeEnum scope = DataScopeEnum.fromValue(dataScopeValue);
        if (scope == null || scope == DataScopeEnum.ALL) {
            return null;
        }

        Expression permissionExpr = buildUserDeptExpression(deptId, scope);
        if (permissionExpr == null) return null;

        return where != null ? new AndExpression(where, permissionExpr) : permissionExpr;
    }

    private boolean isTargetMapper(String msId) {
        for (String prefix : TARGET_MAPPERS) {
            if (msId.startsWith(prefix)) return true;
        }
        return false;
    }

    private Expression buildUserDeptExpression(Long deptId, DataScopeEnum scope) {
        if (deptId == null) return null;

        // 主表 user_id 字段
        Column userIdCol = new Column("user_id");

        // 构建: SELECT user_id FROM sys_user_dept WHERE ...
        PlainSelect userDeptSelect = new PlainSelect();
        userDeptSelect.addSelectItems(new Column("user_id"));
        userDeptSelect.setFromItem(new Table("sys_user_dept"));

        if (scope == DataScopeEnum.DEPT_AND_CHILDREN) {
            // 构建部门子查询: SELECT id FROM sys_dept WHERE id=? OR tree_path LIKE '%,?,%'
            PlainSelect deptSelect = new PlainSelect();
            deptSelect.addSelectItems(new Column("id"));
            deptSelect.setFromItem(new Table("sys_dept"));

            EqualsTo idEq = new EqualsTo();
            idEq.setLeftExpression(new Column("id"));
            idEq.setRightExpression(new LongValue(deptId));

            LikeExpression like = new LikeExpression();
            like.setLeftExpression(new Column("tree_path"));
            like.setRightExpression(new StringValue("%," + deptId + ",%"));

            OrExpression or = new OrExpression(idEq, like);
            deptSelect.setWhere(or);

            // 关键：用 ParenthesedSelect 包装子查询
            ParenthesedSelect deptSubQuery = new ParenthesedSelect();
            deptSubQuery.setSelect(deptSelect);

            InExpression inDept = new InExpression();
            inDept.setLeftExpression(new Column("dept_id"));
            inDept.setRightExpression(deptSubQuery); // ← 直接传 ParenthesedSelect

            userDeptSelect.setWhere(inDept);

        } else if (scope == DataScopeEnum.DEPT) {
            EqualsTo eq = new EqualsTo();
            eq.setLeftExpression(new Column("dept_id"));
            eq.setRightExpression(new LongValue(deptId));
            userDeptSelect.setWhere(eq);
        } else {
            return null;
        }

        // 最终子查询：(SELECT user_id FROM sys_user_dept WHERE ...)
        ParenthesedSelect userDeptSubQuery = new ParenthesedSelect();
        userDeptSubQuery.setSelect(userDeptSelect);

        // 构建: user_id IN (...)
        InExpression finalIn = new InExpression();
        finalIn.setLeftExpression(userIdCol);
        finalIn.setRightExpression(userDeptSubQuery); // ← 这里也用 ParenthesedSelect

        return finalIn;
    }
}