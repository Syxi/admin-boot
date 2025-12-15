package com.admin.common.context;

import com.admin.common.aspect.LogBatchProcessor;
import com.admin.module.system.service.SysUserService;
import com.admin.module.system.service.UserLoginLogService;
import com.admin.module.system.service.UserOperationLogService;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order
@Component
public class BaseServiceBeanContext {

    public static SysUserService sysUserService;

    public static UserOperationLogService userOperationLogService;

    public static UserLoginLogService userLoginLogService;
    
    public static ApplicationContext applicationContext;

    public static LogBatchProcessor logBatchProcessor;

    @Resource
    public void setSysUserService(SysUserService sysUserService) {
        BaseServiceBeanContext.sysUserService = sysUserService;
    }

    @Resource
    public void setUserOperationLogService(UserOperationLogService userOperationLogService) {
        BaseServiceBeanContext.userOperationLogService = userOperationLogService;
    }

    @Resource
    public void setUserLoginLogService(UserLoginLogService userLoginLogService) {
        BaseServiceBeanContext.userLoginLogService = userLoginLogService;
    }
    
    @Resource
    public void setApplicationContext(ApplicationContext applicationContext) {
        BaseServiceBeanContext.applicationContext = applicationContext;
    }

    @Resource
    public void setLogBatchProcessor(LogBatchProcessor logBatchProcessor) {
        BaseServiceBeanContext.logBatchProcessor = logBatchProcessor;
    }
}