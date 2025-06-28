package com.admin.module.system.service;

import com.admin.module.system.query.UserOperationLogQuery;
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
     * @return
     */
    IPage<UserOperationLog> selectUserOperationLogPage(UserOperationLogQuery query);


    /**
     * 保存日志信息
     * @param joinPoint
     */
    void saveUserOperationLog(JoinPoint joinPoint);
}
