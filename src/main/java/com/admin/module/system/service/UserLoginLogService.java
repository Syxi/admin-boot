package com.admin.module.system.service;

import com.admin.module.system.entity.UserLoginLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.aspectj.lang.JoinPoint;

/**
* @author sy
* @description 针对表【user_login_log】的数据库操作Service
* @createDate 2024-05-24 14:55:49
*/
public interface UserLoginLogService extends IService<UserLoginLog> {


    /**
     * 登录日志分页列表
     *
     * @param page
     * @param limit
     * @param username
     * @return
     */
    IPage<UserLoginLog> selectLoginLogPage(Integer page, Integer limit, String username);

    /**
     * 保存登录日志
     * @param joinPoint
     */
    void saveLoginLog(JoinPoint joinPoint);
}
