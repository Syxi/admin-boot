package com.admin.common.context;

import com.admin.common.aspect.LogBatchProcessor;
import com.admin.module.system.service.*;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order
@Component
public class BaseServiceBeanContext {

    public static SysUserService sysUserService;

    public static UserOperationLogService userOperationLogService;

    public static UserLoginLogService userLoginLogService;

    public static LogBatchProcessor logBatchProcessor;

    public static SysMenuService sysMenuService;


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
    public void setLogBatchProcessor(LogBatchProcessor logBatchProcessor) {
        BaseServiceBeanContext.logBatchProcessor = logBatchProcessor;
    }

    @Resource
    public void setSysMenuService(SysMenuService sysMenuService) {
        BaseServiceBeanContext.sysMenuService = sysMenuService;
    }

}