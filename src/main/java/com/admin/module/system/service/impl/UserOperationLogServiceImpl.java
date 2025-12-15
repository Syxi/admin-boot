package com.admin.module.system.service.impl;

import com.admin.common.context.BaseServiceBeanContext;
import com.admin.common.util.IpUtil;
import com.admin.module.system.entity.UserOperationLog;
import com.admin.module.system.mapper.UserOperationLogMapper;
import com.admin.module.system.query.UserOperationLogQuery;
import com.admin.module.system.service.UserOperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
* @author sy
* @description 针对表【user_operation_log】的数据库操作Service实现
* @createDate 2024-04-29 16:17:32
*/
@RequiredArgsConstructor
@Service
public class UserOperationLogServiceImpl extends ServiceImpl<UserOperationLogMapper, UserOperationLog>
    implements UserOperationLogService {


    /**
     * 获取用户操作日志
     *
     * @return
     */
    @Override
    public IPage<UserOperationLog> selectUserOperationLogPage(UserOperationLogQuery query) {
        LambdaQueryWrapper<UserOperationLog> wrapper = new LambdaQueryWrapper<UserOperationLog>();
        if (StringUtils.isNotBlank(query.getUsername())) {
            wrapper.like(UserOperationLog::getUsername, query.getUsername());
        }
        wrapper.orderByDesc(UserOperationLog::getCreateTime);

        IPage<UserOperationLog> pageInfo = new Page<>(query.getPage(), query.getLimit());
        IPage<UserOperationLog> userOperationLogs = this.page(pageInfo, wrapper);
        return userOperationLogs;
    }

    
    @Override
    public void saveUserOperationLog(JoinPoint joinPoint, Long userId, String username) {
        try {
            HttpServletRequest request = getCurrentHttpRequest();
            if (request == null) {
                log.warn("No HTTP request found, skipping user operation log");
                return;
            }

            saveUserOperationLogInternal(joinPoint, request, userId, username);
        } catch (Exception e) {
            log.error("保存操作日志异常", e);
        }
    }
    
    /**
     * 保存操作日志的核心实现
     * @param joinPoint 切点
     * @param request HTTP请求
     * @param userId 用户ID
     * @param username 用户名
     */
    private void saveUserOperationLogInternal(JoinPoint joinPoint, HttpServletRequest request, Long userId, String username) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName + "()";

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Operation operation = method.getAnnotation(Operation.class);
        String userOperationValue;
        if (operation != null) {
            userOperationValue = operation.summary();
        } else {
            userOperationValue = "系统模块";
        }

        String ip = IpUtil.getIpAddr(request);
        String address = IpUtil.getRegion(ip);

        UserOperationLog userOperationLog = new UserOperationLog();
        userOperationLog.setUserId(userId);
        userOperationLog.setUsername(username);
        userOperationLog.setOperation(userOperationValue);
        userOperationLog.setIp(ip);
        userOperationLog.setAddress(address);
        userOperationLog.setMethod(fullMethodName);
        userOperationLog.setCreateTime(LocalDateTime.now());

        BaseServiceBeanContext.logBatchProcessor.addOperationLog(userOperationLog);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        try {
            RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            if (ra instanceof ServletRequestAttributes) {
                return ((ServletRequestAttributes) ra).getRequest();
            }
        } catch (IllegalStateException e) {
            // No request bound to current thread
        }
        return null;
    }

}




