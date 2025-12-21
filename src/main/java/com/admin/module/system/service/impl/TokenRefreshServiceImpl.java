package com.admin.module.system.service.impl;

import com.admin.common.security.SysUserDetails;
import com.admin.common.security.service.TokenService;
import com.admin.module.system.dto.UserAuthInfo;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.entity.SysUserRole;
import com.admin.module.system.service.SysRoleService;
import com.admin.module.system.service.SysUserRoleService;
import com.admin.module.system.service.SysUserService;
import com.admin.module.system.service.TokenRefreshService;

import com.admin.module.system.vo.AuthTokenVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.admin.common.security.SecurityConstants.ONLINE_USER_PREFIX;

/**
 * Token刷新服务实现类
 * 提供在权限变更时直接刷新用户Token的能力，替代简单的Token失效机制
 */
@Slf4j
@Service
public class TokenRefreshServiceImpl implements TokenRefreshService {
    
    private final TokenService tokenService;
    private final SysUserService userService;
    private final SysUserRoleService userRoleService;
    private final SysRoleService roleService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public TokenRefreshServiceImpl(TokenService tokenService, SysUserService userService, 
                                  SysUserRoleService userRoleService, SysRoleService roleService,
                                  RedisTemplate<String, Object> redisTemplate) {
        this.tokenService = tokenService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 为指定用户刷新Token
     * 
     * @param username 用户名
     * @return 新的认证Token信息
     */
    @Override
    public AuthTokenVO refreshTokenForUser(String username) {
        try {
            // 获取用户最新的认证信息
            UserAuthInfo userAuthInfo = userService.getUserAuthInfo(username);
            SysUserDetails sysUserDetails = new SysUserDetails(userAuthInfo);
            Authentication authentication = new UsernamePasswordAuthenticationToken(sysUserDetails, "", sysUserDetails.getAuthorities());
            
            // 生成新的Token
            AuthTokenVO newToken = tokenService.generateToken(authentication);
            
            // 更新在线用户记录
            updateOnlineUserRecord(username, newToken.getAccessToken());
            
            log.info("为用户刷新Token成功: username={}", username);
            return newToken;
        } catch (Exception e) {
            log.error("为用户刷新Token失败: username={}", username, e);
            throw new RuntimeException("Token刷新失败", e);
        }
    }
    
    /**
     * 为指定认证信息生成新的Token
     * 
     * @param authentication 认证信息
     * @return 新的认证Token信息
     */
    @Override
    public AuthTokenVO generateNewToken(Authentication authentication) {
        return tokenService.generateToken(authentication);
    }
    
    /**
     * 刷新拥有指定角色的所有在线用户Token
     * 
     * @param roleCode 角色编码
     */
    @Override
    public void refreshOnlineUsersByRole(String roleCode) {
        try {
            // 获取所有在线用户
            Set<String> onlineUserKeys = redisTemplate.keys(ONLINE_USER_PREFIX + "*");
            if (onlineUserKeys == null || onlineUserKeys.isEmpty()) {
                log.debug("当前没有在线用户");
                return;
            }
            
            int refreshedCount = 0;
            for (String key : onlineUserKeys) {
                try {
                    Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);
                    if (userData.isEmpty()) {
                        continue;
                    }
                    
                    String username = (String) userData.get("username");
                    Long userId = (Long) userData.get("userId");
                    
                    // 检查该用户是否拥有被修改的角色
                    boolean hasRole = doesUserHaveRole(userId, roleCode);
                    
                    if (hasRole && username != null) {
                        // 为用户生成新的Token
                        refreshTokenForUser(username);
                        refreshedCount++;
                        log.debug("已为用户刷新Token: userId={}, roleCode={}", userId, roleCode);
                    }
                } catch (Exception e) {
                    log.error("处理在线用户Token刷新失败: key={}", key, e);
                }
            }
            
            log.info("角色权限变更，已为 {} 个在线用户刷新Token: roleCode={}", refreshedCount, roleCode);
        } catch (Exception e) {
            log.error("为在线用户刷新Token失败: roleCode={}", roleCode, e);
        }
    }
    
    /**
     * 更新在线用户记录
     * 
     * @param username 用户名
     * @param newToken 新Token
     */
    private void updateOnlineUserRecord(String username, String newToken) {
        String key = ONLINE_USER_PREFIX + username;
        Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);
        
        if (!userData.isEmpty()) {
            // 更新Token
            userData.put("token", newToken);
            // 更新登录时间
            userData.put("loginTime", System.currentTimeMillis());
            
            // 重新存储
            redisTemplate.opsForHash().putAll(key, userData);
        }
    }
    
    /**
     * 检查用户是否拥有指定角色
     * 
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否拥有角色
     */
    private boolean doesUserHaveRole(Long userId, String roleCode) {
        // 获取用户的所有角色ID
        List<SysUserRole> userRoles = userRoleService.selectUserRoleList(userId);
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        
        // 检查是否有匹配的角色编码
        return userRoles.stream()
                .map(SysUserRole::getRoleId)
                .anyMatch(roleId -> {
                    // 通过角色服务获取角色信息
                    SysRole role = roleService.getById(roleId);
                    return role != null && roleCode.equals(role.getRoleCode());
                });
    }
}