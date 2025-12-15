package com.admin.module.system.service.impl;

import com.admin.common.exception.CustomException;
import com.admin.common.properties.SecurityProperties;
import com.admin.common.security.SecurityConstants;
import com.admin.common.security.SysUserDetails;
import com.admin.common.security.service.TokenService;
import com.admin.module.system.query.OnlineUserQuery;
import com.admin.module.system.service.OnlineUserService;
import com.admin.module.system.vo.OnlineUserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

/**
* @author sy
* @description 针对表【t_online_user(在线用户表)】的数据库操作Service实现
* @createDate 2025-07-10 17:59:00
*/
@Slf4j
@Service
public class OnlineUserServiceImpl implements OnlineUserService{

    private final RedisTemplate<String, Object> redisTemplate;

    private final SecurityProperties securityProperties;

    private final byte[] secretKeyBytes;

    private final TokenService tokenService;

    public OnlineUserServiceImpl(SecurityProperties securityProperties, RedisTemplate<String, Object> redisTemplate, TokenService tokenService) {
        this.securityProperties = securityProperties;
        this.secretKeyBytes = securityProperties.getJwt().getKey().getBytes(StandardCharsets.UTF_8);
        this.redisTemplate = redisTemplate;
        this.tokenService = tokenService;
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
                    tokenService.blacklistToken(oldToken);
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

            // 获取所有符合条件的数据
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

            // 使用 MyBatis-Plus 的分页工具进行分页
            int pageSize = onlineUserQuery.getLimit();
            int pageNum = onlineUserQuery.getPage();
            int total = onlineUserVOList.size();

            // 分页处理
            int fromIndex = Math.min((pageNum - 1) * pageSize, total);
            int toIndex = Math.min(fromIndex + pageSize, total);

            List<OnlineUserVO> pageList = new ArrayList<>();
            if (fromIndex < toIndex) {
                pageList = onlineUserVOList.subList(fromIndex, toIndex);
            }

            // 创建分页对象
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
     * 强制用户下线, 把token加入黑名单
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
                tokenService.blacklistToken(token);
            }

        } catch (Exception e) {
            log.error("强制用户下线失败：{}", username, e);
            throw new RuntimeException("强制用户下线失败："+ e.getMessage());
        }

    }




}




