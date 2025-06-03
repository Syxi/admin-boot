//package com.admin.common.security;
//
//import com.admin.common.constant.JwtClaimConstants;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.io.DecodingException;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.http.HttpHeaders;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.stereotype.Component;
//
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * JWT token工具类
// * 用于生成|校验|解析 JWT Token
// * @author suYan
// * @date 2023/4/5 12:01
// */
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class JwtUtil {
//
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    /**
//     * 签名密钥，用于签名 Access Token
//     */
//    @Value("${jwt.secretKey}")
//    private String secretKey;
//
//
//    /**
//     * token过期时间
//     */
//    @Value("${jwt.expire}")
//    private int expiration;
//
//
//    /**
//     * Base64 编码后的签名密钥，用于校验|解析 Access Token
//     */
//    private byte[] secretKeyBytes;
//
//
//    /**
//     * 初始化方法
//     * 对签名密钥进行 Base64 编码
//     */
//    @PostConstruct
//    protected void init() {
//        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
//    }
//
//
//
//    /**
//     * 生成 token令牌
//     * 认证成功后的用户信息会被封装到 Authentication 对象中， 然后通过 createToken() 方法创建 token 字符串
//     * @param authentication 用户认证信息
//     * @return token 字符串
//     */
//    public String createToken(Authentication authentication) {
//        Claims claims = Jwts.claims().setSubject(authentication.getName());
//       SysUserDetails sysUserDetails = (SysUserDetails) authentication.getPrincipal();
//        claims.put(JwtClaimConstants.USER_ID, sysUserDetails.getUserId());
//        claims.put(JwtClaimConstants.USER_NAME, claims.getSubject());
//
//        // claims 中添加角色信息
//        Set<String> roles = sysUserDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toSet());
//        claims.put(JwtClaimConstants.ROLES, roles);
//
//        // jti  jwt的id
//        String jti = UUID.randomUUID().toString();
//        claims.put(JwtClaimConstants.JTI, jti);
//
//        // 过期时间
//        Date now = new Date();
//        Date expirationTime = new Date(now.getTime() + expiration * 1000L);
//        return Jwts.builder()
//                .setClaims(claims)
//                .setId(jti)
//                .setIssuedAt(now)
//                .setExpiration(expirationTime)
//                .signWith(Keys.hmacShaKeyFor(getSecretKeyBytes()), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//
//
//    /**
//     * 从请求头解析获取 Token
//     * @param request
//     * @return
//     */
//    public String resolveToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//             return bearerToken.substring(7);
//        }
//        return null;
//    }
//
//
//    /**
//     * 校验 token 是否有效
//     * @param token
//     * @return 是否有效
//     */
//    public boolean validateToken(String token) {
//        Jws<Claims> claimsJws = Jwts.parserBuilder()
//                .setSigningKey(this.getSecretKeyBytes())
//                .build()
//                .parseClaimsJws(token);
//
//        Claims body = claimsJws.getBody();
//        // 校验是否过期
//        Date expiration = body.getExpiration();
//        // false 已过期
//        boolean isValid = expiration.before(new Date());
//
//        // 检查 token 是否已被加入黑名单 (注销、修改密码)
//        if (isValid) {
//            Claims claims = getTokenClaims(token);
//            String jti = claims.getId();
//            boolean isBlockedList = Boolean.TRUE.equals(redisTemplate.hasKey(SecurityConstants.BLACK_TOKEN_PREFIX + jti));
//            // 在黑名单内，返回false，标记token失效
//            if (isBlockedList) {
//                return false;
//            }
//        }
//
//        return isValid;
//    }
//
//
//    /**
//     * 获取 token 的Claims, claims中包含了用户的基本信息
//     */
//    public Claims getTokenClaims(String token) {
//        try {
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(this.getSecretKeyBytes())
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//            return claims;
//        } catch (JwtException e) {
//            log.error("token解析失败：{}", e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    /**
//     * 获取 token中的用户名
//     * @param token
//     * @return
//     */
//    public String getUserName(String token) {
//        return getTokenClaims(token).getSubject();
//    }
//
//
//    /**
//     * 获取签名密钥的字符数组
//     * @return 签名密钥的字节数组
//     */
//    public byte[] getSecretKeyBytes() {
//        if (secretKeyBytes == null) {
//            try {
//                secretKeyBytes = Decoders.BASE64.decode(secretKey);
//            } catch (DecodingException e) {
//                secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
//            }
//        }
//        return secretKeyBytes;
//    }
//
//
//    /**
//     * 根据给定的令牌解析出用户认证信息
//     * @param token JWT token
//     * @return 用户的认证信息
//     */
//    public Authentication getAuthentication(String token) {
//        Claims claims = this.getTokenClaims(token);
//
//        SysUserDetails sysUserDetails = new SysUserDetails();
//        // 用户ID
//        Long userId = claims.get(JwtClaimConstants.USER_ID, Long.class);
//        sysUserDetails.setUserId(userId);
//        // 用户名
//        String username = claims.get(JwtClaimConstants.USER_NAME, String.class);
//        sysUserDetails.setUsername(username);
//
//        // 角色集合
//        List<SimpleGrantedAuthority> authorities = ((ArrayList<String>) claims.get(JwtClaimConstants.ROLES))
//                .stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//
//        return new UsernamePasswordAuthenticationToken(sysUserDetails, "", authorities);
//    }
//
//
//
//
//
//}
