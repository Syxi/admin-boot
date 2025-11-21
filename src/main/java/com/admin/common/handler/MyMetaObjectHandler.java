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
//        Long currentTenantId = SecurityUtils.getTenantId();
//        if (metaObject.hasSetter(TENANT_ID) && currentTenantId != null) {
//            this.strictInsertFill(metaObject, TENANT_ID, Long.class, currentTenantId);
//        }
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
