package com.admin.module.system.service.impl;

import com.admin.common.exception.CustomException;
import com.admin.common.properties.SecurityProperties;
import com.admin.common.security.SecurityConstants;
import com.admin.common.security.SysUserDetails;
import com.admin.module.system.entity.OnlineUser;
import com.admin.module.system.mapper.OnlineUserMapper;
import com.admin.module.system.query.OnlineUserQuery;
import com.admin.module.system.service.OnlineUserService;
import com.admin.module.system.vo.OnlineUserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
* @author sy
* @description 针对表【t_online_user(在线用户表)】的数据库操作Service实现
* @createDate 2025-07-10 17:59:00
*/
@Slf4j
@Service
public class OnlineUserServiceImpl extends ServiceImpl<OnlineUserMapper, OnlineUser> implements OnlineUserService{

    private final RedisTemplate<String, Object> redisTemplate;

    private final SecurityProperties securityProperties;

    private final byte[] secretKeyBytes;

    public OnlineUserServiceImpl(SecurityProperties securityProperties, RedisTemplate<String, Object> redisTemplate) {
        this.securityProperties = securityProperties;
        this.secretKeyBytes = securityProperties.getJwt().getKey().getBytes(StandardCharsets.UTF_8);
        this.redisTemplate = redisTemplate;
    }

    /**
     * 用户登录时记录到在线列表
     *
     * @param authentication 认证信息
     * @param token          当前token
     */
    @Override
    public void addOnlineUser(Authentication authentication, String token) {
        SysUserDetails sysUserDetails = (SysUserDetails) authentication.getPrincipal();

        // 构建在线用户key
        String key = SecurityConstants.ONLINE_USER_PREFIX + sysUserDetails.getUsername();

        try {
            Map<Object, Object> oldTokenData = redisTemplate.opsForHash().entries(key);

            // 清理旧token (如果存在)
            if (!oldTokenData.isEmpty()) {
                String oldToken = (String) oldTokenData.get("token");

                // 将旧Token加入黑名单
                if (oldToken != null && !oldToken.equals(token)) {
                    blacklistToken(oldToken);
                }

                // 删除旧Token
                redisTemplate.delete(key);
            }

            // 创建新在线记录
            Map<String, Object> currentTokenData = new HashMap<>();
            currentTokenData.put("token", token);
            currentTokenData.put("loginTime", System.currentTimeMillis());
            currentTokenData.put("userId", sysUserDetails.getUserId());
            currentTokenData.put("username", sysUserDetails.getUsername());

            // 存储
            redisTemplate.opsForHash().putAll(key, currentTokenData);
            log.info("添加在线用户记录成功：{}", sysUserDetails.getUsername());
        } catch (Exception e) {
            log.error("添加在线用户记录失败", e);
            throw new RuntimeException("添加在线用户记录失败："+ e.getMessage());
        }
    }


    /**
     * 刷新Token时 更新在线用户记录
     *
     * @param username 用户名
     * @param newAccessToken   新的AccessToken
     */
    public void updateOnlineUser(String username, String newAccessToken) {
        String key = SecurityConstants.ONLINE_USER_PREFIX + username;

        try {
            Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);
            if (userData.isEmpty()) {
                log.warn("在线用户记录不存在： {}", username);
                return;
            }

            // 更新token 和 登录时间
            redisTemplate.opsForHash().put(key, "token", newAccessToken);
            redisTemplate.opsForHash().put(key, "loginTime", System.currentTimeMillis());
            log.info("更新在线用户成功：{}", username);
        } catch (Exception e) {
            log.error("更新在线用户记录失败：{}", username, e);
            throw new RuntimeException("更新在线用户记录失败："+ e.getMessage());
        }
    }




    /**
     * 查询在线用户列表
     *
     * @param onlineUserQuery
     * @return
     */
    @Override
    public IPage<OnlineUserVO> selectOnlineUserPage(OnlineUserQuery onlineUserQuery) {
        try {
            Set<String> keys = redisTemplate.keys(SecurityConstants.ONLINE_USER_PREFIX + "*");
            if (keys.isEmpty()) {
                return new Page<>();
            }

            List<OnlineUserVO> onlineUserVOList = new ArrayList<>();
            String usernameFilter = onlineUserQuery.getUsername();
            for (String key : keys) {
                Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);
                if (userData.isEmpty()) continue;

                String username = (String) userData.get("username");
                // 按用户名过滤
                if (StringUtils.isNotEmpty(usernameFilter) && !username.contains(usernameFilter)) {
                    continue;
                }

                OnlineUserVO onlineUserVO = new OnlineUserVO();
                onlineUserVO.setUsername(username);
                onlineUserVO.setUserId((Long) userData.get("userId"));
                onlineUserVO.setLoginTime(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli((Long) userData.get("loginTime")),
                        ZoneId.systemDefault()
                ));
                onlineUserVOList.add(onlineUserVO);
            }

            // 手动分页
            int pageSize = onlineUserQuery.getLimit();
            int pageNum = onlineUserQuery.getPage();
            int total = onlineUserVOList.size();

            int fromIndex = Math.min((pageNum-1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);
            List<OnlineUserVO> pageList = onlineUserVOList.subList(fromIndex, toIndex);

            // 创造分页对象
            Page<OnlineUserVO> page = new Page<>(pageNum, pageSize);
            page.setTotal(total);
            page.setRecords(pageList);
            return page;
        } catch (Exception e) {
            log.error("分页查询用户列表异常", e);
            throw new CustomException("查询用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 强制用户下线
     *
     * @param username
     */
    @Override
    public void forceLogout(String username) {
        String key = SecurityConstants.ONLINE_USER_PREFIX + username;

        try {
            Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);

            if (userData.isEmpty()) {
                return;
            }

            String token = (String) userData.get("token");
            // 将token加入黑名单
            if (token != null) {
                blacklistToken(token);
            }

            // 删除在线用户记录
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("强制用户下线失败：{}", username, e);
            throw new RuntimeException("强制用户下线失败："+ e.getMessage());
        }

    }

    /**
     * 将 Token 加入黑名单
     *
     * @param token JWT Token
     */
    private void blacklistToken(String token) {
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

    /**
     * 获取 token 的Claims, claims中包含了用户的基本信息
     */
    private Claims getTokenClaims(String token) {
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


}




