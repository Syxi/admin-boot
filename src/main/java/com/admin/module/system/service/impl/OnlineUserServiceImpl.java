package com.admin.module.system.service.impl;

import com.admin.common.security.SecurityConstants;
import com.admin.common.security.SysUserDetails;
import com.admin.module.system.query.OnlineUserQuery;
import com.admin.module.system.service.OnlineUserService;
import com.admin.module.system.vo.OnlineUserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class OnlineUserServiceImpl implements OnlineUserService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户登录时记录到在线列表
     *
     * @param authentication 认证信息
     * @param token          当前token
     * @param jwt            token的唯一标识符
     */
    @Override
    public void addOnlineUser(Authentication authentication, String token, String jwt) {
        SysUserDetails sysUserDetails = (SysUserDetails) authentication.getPrincipal();
        String username = sysUserDetails.getUsername();

        String key = SecurityConstants.ONLINE_USER_PREFIX + username;

        // 如果已有 Token存在，则将其加入黑名单并删除
        Map<Object, Object> oldTokenData = redisTemplate.opsForHash().entries(key);
        if (!oldTokenData.isEmpty()) {
            String oldToken = (String) oldTokenData.get("token");
            String oldJwt = (String) oldTokenData.get("jwt");

            if (oldToken != null && oldJwt != null) {
                redisTemplate.opsForHash().put(key, "token", oldToken);
                redisTemplate.opsForHash().put(key, "jwt", oldJwt);
                redisTemplate.opsForHash().put(key, "logoutTime", System.currentTimeMillis());
                redisTemplate.opsForHash().put(key, "status", "black");
            }
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
        Set<String> onlineUserList = redisTemplate.keys(SecurityConstants.ONLINE_USER_PREFIX + "*");
        return null;
    }

    /**
     * 强制用户下线
     *
     * @param username
     */
    @Override
    public void forceLogout(String username) {

    }
}
