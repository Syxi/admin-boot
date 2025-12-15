package com.admin.module.system.service.impl;

import com.admin.common.context.BaseServiceBeanContext;
import com.admin.common.util.IpUtil;
import com.admin.module.system.entity.UserLoginLog;
import com.admin.module.system.mapper.UserLoginLogMapper;
import com.admin.module.system.query.LoginLogQuery;
import com.admin.module.system.service.UserLoginLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
* @author sy
* @description 针对表【user_login_log】的数据库操作Service实现
* @createDate 2024-05-24 14:55:49
*/
@RequiredArgsConstructor
@Service
public class UserLoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog>
    implements UserLoginLogService{

    private final HttpServletRequest request;


    /**
     * 登录日志分页列表
     *
     * @return
     */
    @Override
    public IPage<UserLoginLog> selectLoginLogPage(LoginLogQuery loginLogQuery) {
        LambdaQueryWrapper<UserLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(loginLogQuery.getUsername())) {
            wrapper.like(UserLoginLog::getUsername, loginLogQuery.getUsername());
        }
        wrapper.orderByDesc(UserLoginLog::getId);

        IPage<UserLoginLog> pageInfo = new Page<>(loginLogQuery.getPage(), loginLogQuery.getLimit());
        IPage<UserLoginLog> userLoginLogIPage = this.page(pageInfo, wrapper);
        return userLoginLogIPage;
    }

    
    /**
     * 保存用户登录日志（带用户信息）
     *
     * @param joinPoint
     * @param userId
     * @param username
     */
    @Override
    public void saveLoginLog(JoinPoint joinPoint, Long userId, String username) {
        try {
            saveLoginLogInternal(userId, username);
        } catch (Exception e) {
            log.error("保存登录日志异常", e);
        }
    }
    
    /**
     * 保存登录日志的核心实现
     * @param userId 用户ID
     * @param username 用户名
     */
    private void saveLoginLogInternal(Long userId, String username) {
        // ip
        String ip = IpUtil.getIpAddr(request);
        // ip的地址：中国-广东省-广州
        String address = IpUtil.getRegion(ip);

        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        // 操作系统类型
        String os = userAgent.getOperatingSystem().getName();
        // 浏览器类型
        String browser = userAgent.getBrowser().getName();

        UserLoginLog userLoginLog = new UserLoginLog();
        userLoginLog.setUserId(userId);
        userLoginLog.setUsername(username);
        userLoginLog.setIp(ip);
        userLoginLog.setAddress(address);
        userLoginLog.setOs(os);
        userLoginLog.setBrowser(browser);
        userLoginLog.setLoginTime(LocalDateTime.now());
        BaseServiceBeanContext.logBatchProcessor.addLoginLog(userLoginLog);
    }
}