package com.admin.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.admin.common.security.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author suYan
 * @date 2023/4/1 20:04
 * 自动填充创建时间、创建userId、更新时间、更新userId、租户id
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final String TENANT_ID = "tenantId";

    /**
     * 新增填充创建时间、创建userId
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "createUser", Long.class, SecurityUtils.getUserId());

        // 自动填充tenantId字段
        if (metaObject.hasSetter(TENANT_ID)) {
            // 获取当前租户ID
            Long currentTenantId = SecurityUtils.getTenantId();
            
            // 对于admin用户，即使没有特定租户ID，我们也需要设置一个合适的租户ID
            // 以便数据可以被正确查询
            if (SecurityUtils.isAdmin() && currentTenantId == null) {
                // 对于admin用户，如果没有特定租户ID，可以选择设置为0或其他特殊值
                // 或者让其保持为null（在这种情况下，需要确保查询逻辑能正确处理）
                // 为了让admin用户创建的数据能够被正确查询到，我们将其设置为0（或可选的全局租户ID）
                this.strictInsertFill(metaObject, TENANT_ID, Long.class, 0L); // 使用0作为admin用户的租户ID
            } else if (currentTenantId != null) {
                // 如果当前租户ID不为null，则填充
                this.strictInsertFill(metaObject, TENANT_ID, Long.class, currentTenantId);
            }
            // 如果非admin用户且currentTenantId为null，则不填充tenantId
        }
    }

    /**
     * 更新填充更新时间、更新userId
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updateUser", Long.class, SecurityUtils.getUserId());
    }


}
