package com.admin.common.aspect;

import com.admin.module.system.service.UserLoginLogService;
import com.admin.module.system.service.UserOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 日志切面 - 用于记录用户操作日志和登录日志
 * 
 * @author suYan
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final UserOperationLogService userOperationLogService;
    private final UserLoginLogService userLoginLogService;

    /**
     * 定义用户操作日志切点
     * 拦截 com.admin.web 包及其子包的所有方法
     * 排除 AuthController 和 WebSocketController
     */
    @Pointcut("execution(* com.admin.web..*.*(..)) " +
            "&& !within(com.admin.web.AuthController)" +
            "&& !within(com.admin.web.WebSocketController)")
    public void operationLogPointcut() {}

    /**
     * 方法正常返回后记录操作日志
     *
     * @param joinPoint 连接点
     */
    @AfterReturning("operationLogPointcut()")
    public void saveOperationLogAfter(JoinPoint joinPoint) {
        try {
            userOperationLogService.saveUserOperationLog(joinPoint);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 定义登录日志切点
     * 拦截获取当前用户信息的方法，在执行该接口后才有用户信息
     */
    @Pointcut("execution(* com.admin.web.SysUserController.getCurrentUserInfo(..))")
    public void loginLogPointcut() {}

    /**
     * 方法正常返回后记录登录日志
     *
     * @param joinPoint 连接点
     */
    @AfterReturning("loginLogPointcut()")
    public void saveLoginLogAfter(JoinPoint joinPoint) {
        try {
            userLoginLogService.saveLoginLog(joinPoint);
        } catch (Exception e) {
            log.error("保存登录日志失败", e);
        }
    }
}
