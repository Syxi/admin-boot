package com.admin.common.aspect;

import com.admin.module.system.service.UserLoginLogService;
import com.admin.module.system.service.UserOperationLogService;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 用户操作日志
 */
@Aspect
@Component
@AllArgsConstructor
public class LogAspect {

    private final UserOperationLogService userOperationLogService;

    private final UserLoginLogService userLoginLogService;



    /**
     * 定义用户操作日志切点，拦截 com.yan.web.controller 包和子包的所有方法。登录AuthController不拦截
     */
    @Pointcut("execution(* com.admin.web..*.*(..)) " +
            "&& !within(com.admin.web.AuthController)" +
            "&& !within(com.admin.web.WebSocketController)")
    public void operationLogPointcut()  {}


    /**
     * 用户操作日志
     * @param joinPoint
     * @throws Throwable
     */
    @AfterReturning("operationLogPointcut()")
    public void saveOperationLogAfter(JoinPoint joinPoint) throws Throwable {
        userOperationLogService.saveUserOperationLog(joinPoint);
    }


    /**
     * 定义登录日志切点
     * 执行该接口，才有用户的信息，不然SecurityUtils.getUserId()为空
     */
    @Pointcut("execution(* com.admin.web.SysUserController.getCurrentUserInfo(..))")
    public void loginLogPointcut() {}

    /**
     * 登录日志
     * @param joinPoint
     */
    @AfterReturning("loginLogPointcut()")
    public void saveLoginLogAfter(JoinPoint joinPoint) {
        userLoginLogService.saveLoginLog(joinPoint);
    }












}
