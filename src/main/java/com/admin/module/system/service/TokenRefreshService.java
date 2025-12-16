package com.admin.module.system.service;

import com.admin.module.system.vo.AuthTokenVO;
import org.springframework.security.core.Authentication;

/**
 * Token刷新服务接口
 * 提供在权限变更时直接刷新用户Token的能力，替代简单的Token失效机制
 */
public interface TokenRefreshService {
    
    /**
     * 为指定用户刷新Token
     * 
     * @param username 用户名
     * @return 新的认证Token信息
     */
    AuthTokenVO refreshTokenForUser(String username);
    
    /**
     * 为指定认证信息生成新的Token
     * 
     * @param authentication 认证信息
     * @return 新的认证Token信息
     */
    AuthTokenVO generateNewToken(Authentication authentication);
    
    /**
     * 刷新拥有指定角色的所有在线用户Token
     * 
     * @param roleCode 角色编码
     */
    void refreshOnlineUsersByRole(String roleCode);
}