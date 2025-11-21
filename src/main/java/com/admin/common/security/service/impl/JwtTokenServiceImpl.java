package com.admin.common.security.service.impl;

import com.admin.common.constant.JwtClaimConstants;
import com.admin.common.exception.CustomException;
import com.admin.common.properties.SecurityProperties;
import com.admin.common.result.ResultCode;
import com.admin.common.security.SecurityConstants;
import com.admin.common.security.SysUserDetails;
import com.admin.common.security.service.TokenService;
import com.admin.module.system.vo.AuthTokenVO;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JWT Token 服务实现类
 * 负责JWT Token的生成、解析、校验、刷新和黑名单管理
 * 
 * @author suYan
 */
@Slf4j
@ConditionalOnProperty(value = "security.session.type", havingValue = "jwt")
@Service
public class JwtTokenServiceImpl implements TokenService {

    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final byte[] secretKeyBytes;

    public JwtTokenServiceImpl(SecurityProperties securityProperties, RedisTemplate<String, Object> redisTemplate) {
        this.securityProperties = securityProperties;
        this.redisTemplate = redisTemplate;
        this.secretKeyBytes = securityProperties.getJwt().getKey().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 生成认证Token（包括AccessToken和RefreshToken）
     *
     * @param authentication 用户认证信息
     * @return Token响应对象
     */
    @Override
    public AuthTokenVO generateToken(Authentication authentication) {
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
     * 创建JWT Token
     *
     * @param authentication 认证信息
     * @param ttl 过期时间（秒）
     * @return JWT Token字符串
     */
    private String createToken(Authentication authentication, Long ttl) {
        SysUserDetails userDetails = (SysUserDetails) authentication.getPrincipal();
        Date now = new Date();
        String jti = UUID.randomUUID().toString();

        // 构建Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimConstants.USER_ID, userDetails.getUserId());
        claims.put(JwtClaimConstants.USER_NAME, authentication.getName());
        claims.put(JwtClaimConstants.JTI, jti);
        
        // 添加角色信息
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        claims.put(JwtClaimConstants.ROLES, roles);

        // 构建JWT
        JwtBuilder jwtBuilder = Jwts.builder()
                .setClaims(claims)
                .setSubject(authentication.getName())
                .setId(jti)
                .setIssuedAt(now)
                .signWith(SignatureAlgorithm.HS256, secretKeyBytes);

        // 设置过期时间
        if (ttl != null && ttl > 0) {
            Date expirationTime = new Date(now.getTime() + ttl * 1000L);
            jwtBuilder.setExpiration(expirationTime);
        }

        return jwtBuilder.compact();
    }

    /**
     * 获取Token的Claims
     *
     * @param token JWT Token
     * @return Claims对象
     */
    @Override
    public Claims getTokenClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKeyBytes)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("Token解析失败：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析Token获取认证信息
     *
     * @param token JWT Token
     * @return Authentication对象
     */
    @Override
    public Authentication parseToken(String token) {
        Claims claims = this.getTokenClaims(token);

        SysUserDetails sysUserDetails = new SysUserDetails();
        sysUserDetails.setUserId(claims.get(JwtClaimConstants.USER_ID, Long.class));
        sysUserDetails.setUsername(claims.get(JwtClaimConstants.USER_NAME, String.class));

        // 角色集合
        @SuppressWarnings("unchecked")
        List<SimpleGrantedAuthority> authorities = ((ArrayList<String>) claims.get(JwtClaimConstants.ROLES))
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        sysUserDetails.setAuthorities(authorities);

        return new UsernamePasswordAuthenticationToken(sysUserDetails, "", authorities);
    }

    /**
     * 校验Token是否有效
     *
     * @param token JWT Token
     * @return true-有效 false-无效
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyBytes)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String jti = claims.getId();
            Date expiration = claims.getExpiration();

            // 检查是否过期
            boolean isExpire = expiration.before(new Date());
            if (isExpire) {
                return false;
            }

            // 检查是否在黑名单中
            boolean isBlackToken = Boolean.TRUE.equals(redisTemplate.hasKey(SecurityConstants.BLACK_TOKEN_PREFIX + jti));
            if (isBlackToken) {
                log.info("Token在黑名单中: {}", jti);
                return false;
            }

            return true;
        } catch (JwtException e) {
            log.error("Token校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新令牌
     * @return 新的Token响应对象
     */
    @Override
    public AuthTokenVO refreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyBytes)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            Date expiration = claims.getExpiration();
            boolean isExpire = expiration.after(new Date());
            if (!isExpire) {
                throw new CustomException(ResultCode.REFRESH_TOKEN_INVALID.getMsg());
            }

            Authentication authentication = this.parseToken(refreshToken);
            Long accessTokenExpiration = securityProperties.getJwt().getAccessTokenTimeTOLive();
            String newAccessToken = this.createToken(authentication, accessTokenExpiration);

            return AuthTokenVO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType(SecurityConstants.JWT_TOKEN_TYPE)
                    .expires(accessTokenExpiration)
                    .build();
                    
        } catch (JwtException e) {
            log.error("刷新Token解析失败: {} {}", refreshToken, e.getMessage());
            throw new CustomException(ResultCode.REFRESH_TOKEN_INVALID.getMsg());
        }
    }

    /**
     * 将Token加入黑名单
     *
     * @param token JWT Token
     */
    @Override
    public void blacklistToken(String token) {
        Long expiration = securityProperties.getJwt().getAccessTokenTimeTOLive();
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyBytes)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String jti = claims.getId();
            String username = claims.get(JwtClaimConstants.USER_NAME, String.class);
            
            // 删除在线用户记录
            redisTemplate.delete(SecurityConstants.ONLINE_USER_PREFIX + username);
            // 将Token加入黑名单
            redisTemplate.opsForValue().set(SecurityConstants.BLACK_TOKEN_PREFIX + jti, null, expiration, TimeUnit.SECONDS);
            
        } catch (ExpiredJwtException e) {
            // Token已过期，仍需清理在线用户和加入黑名单
            Claims claims = e.getClaims();
            String username = claims.get(JwtClaimConstants.USER_NAME, String.class);
            String jti = claims.getId();
            
            redisTemplate.delete(SecurityConstants.ONLINE_USER_PREFIX + username);
            redisTemplate.opsForValue().set(SecurityConstants.BLACK_TOKEN_PREFIX + jti, null, expiration, TimeUnit.SECONDS);
            log.info("Token已过期，删除在线用户记录：{}", username);
            
        } catch (JwtException e) {
            log.error("Token解析失败：{}", e.getMessage());
            throw new CustomException(ResultCode.TOKEN_INVALID.getMsg());
        }
    }





}
