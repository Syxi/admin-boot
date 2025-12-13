package com.admin.common.security.service.impl;

import com.admin.common.constant.JwtClaimConstants;
import com.admin.common.exception.CustomException;
import com.admin.common.properties.SecurityProperties;
import com.admin.common.result.ResultCode;
import com.admin.common.security.SecurityConstants;
import com.admin.common.security.SysUserDetails;
import com.admin.common.security.service.TokenService;
import com.admin.module.system.vo.AuthTokenVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis Token 服务实现类
 * 负责Redis Token的生成、解析、校验、刷新和黑名单管理
 *
 * @author suYan
 */
@Slf4j
@ConditionalOnProperty(value = "security.session.type", havingValue = "redis-token")
@Service
public class RedisTokenServiceImpl implements TokenService {

    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTokenServiceImpl(SecurityProperties securityProperties, RedisTemplate<String, Object> redisTemplate) {
        this.securityProperties = securityProperties;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成认证Token（包括AccessToken和RefreshToken）
     *
     * @param authentication 用户认证信息
     * @return Token响应对象
     */
    @Override
    public AuthTokenVO generateToken(Authentication authentication) {
        // 对于Redis Token，我们可以使用相同的过期时间配置，但可以考虑区分访问Token和刷新Token
        long accessTokenTtl = securityProperties.getJwt().getAccessTokenTimeTOLive();
        long refreshTokenTtl = securityProperties.getJwt().getRefreshTokenTimeTOLive();

        String accessToken = createToken(authentication, accessTokenTtl);
        String refreshToken = createToken(authentication, refreshTokenTtl);

        return AuthTokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(SecurityConstants.JWT_TOKEN_TYPE)
                .expires(accessTokenTtl)
                .build();
    }

    /**
     * 创建Redis Token
     *
     * @param authentication 认证信息
     * @param ttl            过期时间（秒）
     * @return Token字符串
     */
    private String createToken(Authentication authentication, Long ttl) {
        SysUserDetails userDetails = (SysUserDetails) authentication.getPrincipal();
        String tokenId = UUID.randomUUID().toString();

        // 构建Token信息
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put(JwtClaimConstants.USER_ID, userDetails.getUserId());
        tokenInfo.put(JwtClaimConstants.USER_NAME, authentication.getName());
        tokenInfo.put(JwtClaimConstants.TENANT_ID, userDetails.getDeptId());
        tokenInfo.put(JwtClaimConstants.DATA_SCOPE, userDetails.getDataScope());
        tokenInfo.put(JwtClaimConstants.JTI, tokenId);

        // 添加角色信息
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        tokenInfo.put(JwtClaimConstants.ROLES, roles);

        // 存储到Redis
        String tokenKey = SecurityConstants.BLACK_TOKEN_PREFIX + tokenId;
        if (ttl != null && ttl > 0) {
            redisTemplate.opsForValue().set(tokenKey, tokenInfo, ttl, TimeUnit.SECONDS);
            log.debug("创建Token成功: token={}, ttl={}s, username={}", tokenId, ttl, authentication.getName());
        } else {
            redisTemplate.opsForValue().set(tokenKey, tokenInfo);
            log.debug("创建Token成功（无过期时间）: token={}, username={}", tokenId, authentication.getName());
        }

        return tokenId;
    }

    /**
     * 获取Token的信息
     *
     * @param token Token ID
     * @return Token信息Map
     */
    @Override
    public Claims getTokenClaims(String token) {
        try {
            String tokenKey = SecurityConstants.BLACK_TOKEN_PREFIX + token;
            Map<String, Object> tokenInfo = (Map<String, Object>) redisTemplate.opsForValue().get(tokenKey);
            
            if (tokenInfo == null) {
                return null;
            }
            
            // 使用Jwts工厂方法创建Claims对象
            Claims claims = Jwts.claims();
            claims.putAll(tokenInfo);
            return claims;
        } catch (Exception e) {
            log.error("Token解析失败：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析Token获取认证信息
     *
     * @param token Token ID
     * @return Authentication对象
     */
    @Override
    public Authentication parseToken(String token) {
        Map<String, Object> tokenInfo = this.getTokenClaims(token);

        if (tokenInfo == null) {
            throw new CustomException("Token不存在或已过期");
        }

        SysUserDetails sysUserDetails = new SysUserDetails();
        sysUserDetails.setUserId((Long) tokenInfo.get(JwtClaimConstants.USER_ID));
        sysUserDetails.setUsername((String) tokenInfo.get(JwtClaimConstants.USER_NAME));
        sysUserDetails.setDeptId((Long) tokenInfo.get(JwtClaimConstants.TENANT_ID));
        sysUserDetails.setDataScope((Integer) tokenInfo.get(JwtClaimConstants.DATA_SCOPE));

        // 角色集合 - 注意：从Redis中取出时可能是HashSet，需要正确处理
        Object rolesObj = tokenInfo.get(JwtClaimConstants.ROLES);
        List<SimpleGrantedAuthority> authorities;
        
        if (rolesObj instanceof Set) {
            // 如果是从Redis中取出的HashSet
            @SuppressWarnings("unchecked")
            Set<String> rolesSet = (Set<String>) rolesObj;
            authorities = rolesSet.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else if (rolesObj instanceof List) {
            // 如果是List（向后兼容）
            @SuppressWarnings("unchecked")
            List<String> rolesList = (List<String>) rolesObj;
            authorities = rolesList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            // 兜底处理
            authorities = new ArrayList<>();
        }
        
        sysUserDetails.setAuthorities(authorities);

        return new UsernamePasswordAuthenticationToken(sysUserDetails, "", authorities);
    }

    /**
     * 校验Token是否有效
     *
     * @param token Token ID
     * @return true-有效 false-无效
     */
    @Override
    public boolean validateToken(String token) {
        try {
            String tokenKey = SecurityConstants.BLACK_TOKEN_PREFIX + token;
            Boolean hasKey = redisTemplate.hasKey(tokenKey);

            if (hasKey == null || !hasKey) {
                log.info("Token不存在或已过期: {}", token);
                return false;
            }

            // 检查Token是否过期（通过TTL）
            Long ttl = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
            log.debug("Token TTL检查: token={}, ttl={}s", token, ttl);
            
            if (ttl != null && ttl <= 0) {
                log.info("Token已过期: {}", token);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Token校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新令牌ID
     * @return 新的Token响应对象
     */
    @Override
    public AuthTokenVO refreshToken(String refreshToken) {
        try {
            log.debug("开始刷新Token: {}", refreshToken);
            
            // 检查刷新Token是否存在和有效
            if (!validateToken(refreshToken)) {
                // 检查具体原因
                String tokenKey = SecurityConstants.BLACK_TOKEN_PREFIX + refreshToken;
                Boolean hasKey = redisTemplate.hasKey(tokenKey);
                
                if (hasKey == null || !hasKey) {
                    log.warn("刷新Token不存在: {}", refreshToken);
                } else {
                    // 检查Token是否过期（通过TTL）
                    Long ttl = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
                    log.warn("刷新Token TTL状态: token={}, ttl={}s", refreshToken, ttl);
                    
                    if (ttl != null && ttl <= 0) {
                        log.warn("刷新Token已过期: {}", refreshToken);
                    }
                }
                throw new CustomException(ResultCode.REFRESH_TOKEN_INVALID.getMsg());
            }

            // 获取刷新Token的信息
            Claims refreshTokenClaims = this.getTokenClaims(refreshToken);
            if (refreshTokenClaims == null) {
                log.warn("刷新Token信息为空: {}", refreshToken);
                throw new CustomException(ResultCode.REFRESH_TOKEN_INVALID.getMsg());
            }
            
            // 记录Token信息用于调试
            String username = refreshTokenClaims.get(JwtClaimConstants.USER_NAME, String.class);
            log.debug("刷新Token用户信息: username={}, token={}", username, refreshToken);

            // 重新构建认证信息
            SysUserDetails sysUserDetails = new SysUserDetails();
            sysUserDetails.setUserId(refreshTokenClaims.get(JwtClaimConstants.USER_ID, Long.class));
            sysUserDetails.setUsername(username);
            sysUserDetails.setDeptId(refreshTokenClaims.get(JwtClaimConstants.TENANT_ID, Long.class));
            sysUserDetails.setDataScope(refreshTokenClaims.get(JwtClaimConstants.DATA_SCOPE, Integer.class));

            // 角色集合 - 注意：从Redis中取出时可能是HashSet，需要正确处理
            Object rolesObj = refreshTokenClaims.get(JwtClaimConstants.ROLES);
            List<SimpleGrantedAuthority> authorities;
            
            if (rolesObj instanceof Set) {
                // 如果是从Redis中取出的HashSet
                @SuppressWarnings("unchecked")
                Set<String> rolesSet = (Set<String>) rolesObj;
                authorities = rolesSet.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            } else if (rolesObj instanceof List) {
                // 如果是List（向后兼容）
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) rolesObj;
                authorities = rolesList.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            } else {
                // 兜底处理
                authorities = new ArrayList<>();
            }
            
            sysUserDetails.setAuthorities(authorities);

            Authentication authentication = new UsernamePasswordAuthenticationToken(sysUserDetails, "", authorities);

            // 生成新的访问Token
            Long accessTokenExpiration = securityProperties.getJwt().getAccessTokenTimeTOLive();
            String newAccessToken = this.createToken(authentication, accessTokenExpiration);
            
            log.debug("Token刷新成功: oldRefreshToken={}, newAccessToken={}", refreshToken, newAccessToken);

            return AuthTokenVO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType(SecurityConstants.JWT_TOKEN_TYPE)
                    .expires(accessTokenExpiration)
                    .build();

        } catch (CustomException e) {
            // 直接重新抛出自定义异常
            log.error("刷新Token失败（自定义异常）: {} {}", refreshToken, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("刷新Token失败（系统异常）: {} {}", refreshToken, e.getMessage(), e);
            throw new CustomException(ResultCode.REFRESH_TOKEN_INVALID.getMsg());
        }
    }

    /**
     * 将Token加入黑名单（删除Token）
     *
     * @param token Token ID
     */
    @Override
    public void blacklistToken(String token) {
        try {
            String tokenKey = SecurityConstants.BLACK_TOKEN_PREFIX + token;
            Object tokenInfoObj = redisTemplate.opsForValue().get(tokenKey);

            if (tokenInfoObj != null) {
                // 安全地获取用户名
                String username = null;
                if (tokenInfoObj instanceof Map) {
                    Map<String, Object> tokenInfo = (Map<String, Object>) tokenInfoObj;
                    username = (String) tokenInfo.get(JwtClaimConstants.USER_NAME);
                }
                
                // 删除Token
                redisTemplate.delete(tokenKey);
                
                // 删除在线用户记录
                if (username != null) {
                    redisTemplate.delete(SecurityConstants.ONLINE_USER_PREFIX + username);
                    log.info("Token已加入黑名单并清除用户记录: token={}, username={}", token, username);
                } else {
                    log.info("Token已加入黑名单: token={}", token);
                }
            } else {
                log.debug("尝试加入黑名单的Token不存在: {}", token);
            }
        } catch (Exception e) {
            log.error("Token加入黑名单失败：{}", e.getMessage(), e);
            throw new CustomException(ResultCode.TOKEN_INVALID.getMsg());
        }
    }
}