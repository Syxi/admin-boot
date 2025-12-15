package com.admin.common.aspect;

import com.admin.common.context.BaseServiceBeanContext;
import com.admin.common.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.CompletableFuture;

/**
 * 日志切面 - 用于记录用户操作日志和登录日志
 * 
 * @author suYan
 */
@Slf4j
@Aspect
@Component
public class LogAspect {


    /**
     * 定义用户操作日志切点
     * 拦截 com.admin.web 包及其子包的所有方法
     * 排除 AuthController 和 WebSocketController
     * 只拦截带有 @Operation 注解的方法以提高精准度
     */
    @Pointcut("execution(@io.swagger.v3.oas.annotations.Operation * com.admin.web..*.*(..)) " +
            "&& !within(com.admin.web.AuthController)" +
            "&& !within(com.admin.web.WebSocketController)")
    public void operationLogPointcut() {}

    /**
     * 方法正常返回后记录操作日志（异步处理）
     *
     * @param joinPoint 连接点
     */
    @Around("operationLogPointcut()")
    public Object saveOperationLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 提前获取请求上下文
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        
        // 使用数组包装用户信息，以便在Lambda中使用
        Long[] userInfo = new Long[1]; // userId
        String[] usernameHolder = new String[1]; // username
        
        try {
            // 尝试获取当前用户信息
            userInfo[0] = SecurityUtils.getUserId();
            usernameHolder[0] = SecurityUtils.getUserName();
        } catch (Exception e) {
            // 如果无法获取用户信息（如未登录），则保持为null
            log.debug("无法获取当前用户信息，可能是未登录状态");
        }
        
        // 执行目标方法
        Object result = joinPoint.proceed();
        
        // 异步处理操作日志
        CompletableFuture.runAsync(() -> {
            try {
                // 在异步线程中设置请求上下文
                if (requestAttributes != null) {
                    RequestContextHolder.setRequestAttributes(requestAttributes, true);
                }
                
                // 添加日志到队列中（传递提前获取的用户信息）
                BaseServiceBeanContext.userOperationLogService.saveUserOperationLog(joinPoint, userInfo[0], usernameHolder[0]);
            } catch (Exception e) {
                log.error("异步保存操作日志失败", e);
            } finally {
                // 清理请求上下文
                RequestContextHolder.resetRequestAttributes();
            }
        });
        
        return result;
    }

    /**
     * 定义登录日志切点
     * 拦截获取当前用户信息的方法，在执行该接口后才有用户信息
     */
    @Pointcut("execution(* com.admin.module.system.service.impl.AuthServiceImpl.login(..))")
    public void loginLogPointcut() {}

    /**
     * 方法正常返回后记录登录日志（异步处理）
     *
     * @param joinPoint 连接点
     */
    @Around("loginLogPointcut()")
    public Object saveLoginLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 提前获取请求上下文
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        
        // 执行目标方法
        Object result = joinPoint.proceed();
        
        // 方法执行完成后，获取登录用户的ID和用户名
        Long userId = null;
        String username = null;
        try {
            // 登录成功后，尝试获取当前用户信息
            userId = SecurityUtils.getUserId();
            username = SecurityUtils.getUserName();
        } catch (Exception e) {
            // 如果无法获取用户信息，则保持为null
            log.debug("无法获取登录用户信息");
        }
        
        // 使用数组包装用户信息，以便在Lambda中使用
        Long[] userInfo = new Long[1];
        String[] usernameHolder = new String[1];
        userInfo[0] = userId;
        usernameHolder[0] = username;
        
        // 异步处理登录日志
        CompletableFuture.runAsync(() -> {
            try {
                // 在异步线程中设置请求上下文
                if (requestAttributes != null) {
                    RequestContextHolder.setRequestAttributes(requestAttributes, true);
                }
                
                // 添加日志到队列中
                BaseServiceBeanContext.userLoginLogService.saveLoginLog(joinPoint, userInfo[0], usernameHolder[0]);
            } catch (Exception e) {
                log.error("异步保存登录日志失败", e);
            } finally {
                // 清理请求上下文
                RequestContextHolder.resetRequestAttributes();
            }
        });
        
        return result;
    }
}