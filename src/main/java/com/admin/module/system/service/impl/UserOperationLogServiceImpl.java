package com.admin.module.system.service.impl;

import com.admin.module.system.service.UserOperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.common.security.SecurityConstants;
import com.admin.common.util.IpUtil;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.entity.UserOperationLog;
import com.admin.module.system.mapper.UserOperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;

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

    private final HttpServletRequest request;

    /**
     * 获取用户操作日志
     *
     * @param page
     * @param limit
     * @param username
     * @return
     */
    @Override
    public IPage<UserOperationLog> selectUserOperationLogPage(Integer page, Integer limit, String username) {
        LambdaQueryWrapper<UserOperationLog> wrapper = new LambdaQueryWrapper<UserOperationLog>();
        if (StringUtils.isNotBlank(username)) {
            wrapper.like(UserOperationLog::getUsername, username);
        }
        wrapper.orderByDesc(UserOperationLog::getCreateTime);

        IPage<UserOperationLog> pageInfo = new Page<>(page, limit);
        IPage<UserOperationLog> userOperationLogs = this.page(pageInfo, wrapper);
        return userOperationLogs;
    }

    /**
     * 保存日志信息
     *
     * @param joinPoint
     */
    @Override
    public void saveUserOperationLog(JoinPoint joinPoint) {
        Long userId = null;
        String username = null;

        // 非登录请求，即登录成功后的请求，才给 userId username 赋值
        String requestURI = request.getRequestURI();
        if (!SecurityConstants.LOGIN_PATH.equals(requestURI)) {
            userId = SecurityUtils.getUserId();
            username = SecurityUtils.getUserName();
        }

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

        this.save(userOperationLog);

    }
}




