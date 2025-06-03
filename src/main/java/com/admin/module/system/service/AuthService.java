package com.admin.module.system.service;/**
 * @Author: suYan
 * @Date: 2023-09-14
 */

import com.admin.module.system.vo.AuthTokenVO;
import com.admin.module.system.vo.CaptchaVO;
import com.admin.module.system.vo.LoginParams;

/**
 *@author sy
 *@date 2023/9/14 9:06
 */

public interface AuthService {


    /**
     * 登录
     * @return
     */
    AuthTokenVO login(LoginParams loginParams);


    /**
     * 注销
     */
    void logout();


    /**
     * 获取验证码
     * @return
     */
    CaptchaVO getCaptcha();

    /***
     * 刷新token
     * @param refreshToken
     * @return
     */
    AuthTokenVO refreshToken(String refreshToken);
}
