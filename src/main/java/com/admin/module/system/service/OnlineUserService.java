package com.admin.module.system.service;

import com.admin.module.system.entity.OnlineUser;
import com.admin.module.system.query.OnlineUserQuery;
import com.admin.module.system.vo.OnlineUserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.Authentication;

/**
* @author sy
* @description 针对表【t_online_user(在线用户表)】的数据库操作Service
* @createDate 2025-07-10 17:59:00
*/
public interface OnlineUserService extends IService<OnlineUser> {

    /**
     * 用户登录时记录到在线列表
     *
     * @param authentication 认证信息
     * @param token          当前token
     */
    void addOnlineUser(Authentication authentication, String token);

    /**
     * 查询在线用户列表
     * @param onlineUserQuery
     * @return
     */
    IPage<OnlineUserVO> selectOnlineUserPage(OnlineUserQuery onlineUserQuery);

    /**
     * 强制用户下线
     * @param username
     */
    void forceLogout(String username);

    /**
     * 刷新Token时 更新在线用户记录
     *
     * @param username 用户名
     * @param newAccessToken   新的AccessToken
     */
    void updateOnlineUser(String username, String newAccessToken);

}
