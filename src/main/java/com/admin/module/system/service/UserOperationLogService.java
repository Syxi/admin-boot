package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.UserOperationLog;
import org.aspectj.lang.JoinPoint;

/**
* @author sy
* @description 针对表【user_operation_log】的数据库操作Service
* @createDate 2024-04-29 16:17:32
*/
public interface UserOperationLogService extends IService<UserOperationLog> {

    /**
     * 获取用户操作日志
     * @param page
     * @param limit
     * @param username
     * @return
     */
    IPage<UserOperationLog> selectUserOperationLogPage(Integer page, Integer limit, String username);


    /**
     * 保存日志信息
     * @param joinPoint
     */
    void saveUserOperationLog(JoinPoint joinPoint);
}
