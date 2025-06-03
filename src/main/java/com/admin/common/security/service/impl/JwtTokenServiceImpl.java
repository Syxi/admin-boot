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
import io.jsonwebtoken.security.Keys;
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

@Slf4j
@ConditionalOnProperty(value = "security.session.type", havingValue = "jwt")
@Service
public class JwtTokenServiceImpl implements TokenService {

    private final SecurityProperties securityProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final byte[] secretKeyBytes;

    public JwtTokenServiceImpl(SecurityProperties securityProperties, RedisTemplate<String, Object> redisTemplate) {
        this.securityProperties = securityProperties;
        this.secretKeyBytes = securityProperties.getJwt().getKey().getBytes(StandardCharsets.UTF_8);
        this.redisTemplate = redisTemplate;
    }



    /**
     * 生成认证 Token
     *
     * @param authentication 用户认证信息
     * @return 认证 Token 响应
     */
    @Override
    public AuthTokenVO generateToken(Authentication authentication) {
        long accessTokenTimeTOLive = securityProperties.getJwt().getAccessTokenTimeTOLive();
        long refreshTokenTimeTOLive = securityProperties.getJwt().getRefreshTokenTimeTOLive();

        String accessToken = this.createToken(authentication, accessTokenTimeTOLive);
        String refreshToken = this.createToken(authentication, refreshTokenTimeTOLive);
        return AuthTokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(SecurityConstants.JWT_TOKEN_TYPE)
                .expires(accessTokenTimeTOLive)
                .build();
    }

    /**
     * 生成token
     * @param authentication 认证信息
     * @param ttl 过去时间
     * @return
     */
    private String createToken(Authentication authentication, Long ttl) {
        SysUserDetails sysUserDetails = (SysUserDetails) authentication.getPrincipal();
        Claims claims = Jwts.claims().setSubject(authentication.getName());

        // 添加用户信息到 claims
        claims.put(JwtClaimConstants.USER_ID, sysUserDetails.getUserId());
        claims.put(JwtClaimConstants.USER_NAME, claims.getSubject());
        claims.put(JwtClaimConstants.ORG_ID, sysUserDetails.getOrganId());
        claims.put(JwtClaimConstants.DATA_SCOPE, sysUserDetails.getDataScope());

        // claims 中添加角色信息
        Set<String> roles = sysUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        claims.put(JwtClaimConstants.ROLES, roles);

        // jti  jwt的唯一标识符
        String jti = UUID.randomUUID().toString();
        claims.put(JwtClaimConstants.JTI, jti);

        // 当前时间
        Date now = new Date();

        // 构建 JWT
        JwtBuilder jwtBuilder = Jwts.builder()
                .setClaims(claims)
                .setId(jti)
                .setIssuedAt(now);

        // 设置过期时间，-1表示永不过期
        if (ttl != -1) {
            // ttl是秒数，转换为毫秒
            Date expirationTime = new Date(now.getTime() + ttl * 1000L);
            jwtBuilder.setExpiration(expirationTime);
        }

        return jwtBuilder
                .signWith(Keys.hmacShaKeyFor(secretKeyBytes), SignatureAlgorithm.HS256)
                .compact();
    }




    /**
     * 获取 token 的Claims, claims中包含了用户的基本信息
     */
    @Override
    public Claims getTokenClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyBytes)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims;
        } catch (JwtException e) {
            log.error("token解析失败：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * 解析 Token 获取认证信息
     *
     * @param token JWT Token
     * @return 用户认证信息
     */
    @Override
    public Authentication parseToken(String token) {
        Claims claims = this.getTokenClaims(token);


        SysUserDetails sysUserDetails = new SysUserDetails();
        // 用户ID
        Long userId = claims.get(JwtClaimConstants.USER_ID, Long.class);
        sysUserDetails.setUserId(userId);
        // 用户名
        String username = claims.get(JwtClaimConstants.USER_NAME, String.class);
        sysUserDetails.setUsername(username);

        // 角色集合
        List<SimpleGrantedAuthority> authorities = ((ArrayList<String>) claims.get(JwtClaimConstants.ROLES))
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        sysUserDetails.setAuthorities(authorities);

        return new UsernamePasswordAuthenticationToken(sysUserDetails, "", authorities);
    }



    /**
     * 校验 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    @Override
    public boolean validateToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyBytes)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("验证解析失败{}", e.getMessage());
            return false;
        }
        String jti = claims.getId();
        Date expiration = claims.getExpiration();

        boolean isExpire = expiration.after(new Date());
        if (isExpire) {
            boolean isBlackToken = Boolean.TRUE.equals(redisTemplate.hasKey(SecurityConstants.BLACK_TOKEN_PREFIX + jti));
             if (isBlackToken) {
                 return false; // 在黑名单内无效,token已过期
             }
        }

        return isExpire;
    }


    /**
     * 刷新 Token
     *
     * @param refreshToken 刷新令牌
     * @return 认证 Token 响应
     */
    @Override
    public AuthTokenVO refreshToken(String refreshToken) {
        Claims claims ;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyBytes)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        } catch (JwtException e) {
            log.error("refreshToken解析失败{} {}", refreshToken, e.getMessage());
            throw new CustomException(ResultCode.REFRESH_TOKEN_INVALID.getMsg());
        }
        Date expiration = claims.getExpiration();
        boolean isExpire = expiration.after(new Date());
        if (!isExpire) {
            throw  new CustomException(ResultCode.REFRESH_TOKEN_INVALID.getMsg());
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

    }

    /**
     * 将 Token 加入黑名单
     *
     * @param token JWT Token
     */
    @Override
    public void blacklistToken(String token) {
        if (token.startsWith(SecurityConstants.JWT_TOKEN_PREFIX)) {
            token = token.substring(SecurityConstants.JWT_TOKEN_PREFIX.length());
        }

        Claims claims = this.getTokenClaims(token);
        String jti = claims.getId();
        Date expirationDate = claims.getExpiration();

        if (expirationDate != null) {
            long currentTimeSeconds = System.currentTimeMillis() / 1000;

            if (expirationDate.getTime() < currentTimeSeconds) {
                // token已过期，直接返回
                return;
            }
            // 计算token剩余时间，将其加入黑名单
            long expiration = expirationDate.getTime()- System.currentTimeMillis();
            redisTemplate.opsForValue().set(SecurityConstants.BLACK_TOKEN_PREFIX + jti, null, expiration, TimeUnit.SECONDS);
        } else {
            // 永不过期的token，加入黑名单
            redisTemplate.opsForValue().set(SecurityConstants.BLACK_TOKEN_PREFIX + jti, null);
        }

    }





}
