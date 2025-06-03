package com.admin.common.constant;

/**
 * Jwt Claims声明常量
 * JWT Claims 属于 Payload 的一部分, 包含了一些实体(通常指的是用户)的状态和额外的元数据
 * @Author: suYan
 * @Date: 2023-12-14
 */

public class JwtClaimConstants {

    /**
     * 用户id
     */
    public static final String USER_ID = "userId";

    /**
     * 用户名
     */
    public static final String USER_NAME = "username";

    /**
     * 权限角色code集合
     */
    public static final String ROLES = "roles";

    /**
     * jwt的id
     */
    public static final String JTI = "jti";

    /**
     * 组织id
     */
    public static final String ORG_ID = "orgId";

    /**
     * 数据权限范围
     */
    public static final String DATA_SCOPE = "dataScope";

}
