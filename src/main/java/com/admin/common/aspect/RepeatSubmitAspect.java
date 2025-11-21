package com.admin.common.aspect;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.constant.SystemConstants;
import com.admin.common.exception.CustomException;
import com.admin.common.security.SecurityConstants;
import com.admin.common.security.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 防止重复提交切面
 * 使用Redisson分布式锁防止同一用户对同一接口的重复提交
 *
 * @author suYan
 * @date 2023-12-07
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RepeatSubmitAspect {

    private final RedissonClient redissonClient;
    private final TokenService tokenService;

    private static final String RESUBMIT_LOCK_PREFIX = "LOCK:RESUBMIT:";

    /**
     * 定义防重复提交切点
     * 
     * @param noRepeatSubmit 防重复提交注解
     */
    @Pointcut("@annotation(noRepeatSubmit)")
    public void preventDuplicateSubmitPointCut(NoRepeatSubmit noRepeatSubmit) {
    }

    /**
     * 环绕通知，处理防重复提交逻辑
     * 
     * @param joinPoint 连接点
     * @param preventDuplicateSubmit 防重复提交注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("preventDuplicateSubmitPointCut(preventDuplicateSubmit)")
    public Object doAround(ProceedingJoinPoint joinPoint, NoRepeatSubmit preventDuplicateSubmit) throws Throwable {
        String resubmitLockKey = generateResubmitLockKey();
        
        if (StringUtils.isBlank(resubmitLockKey)) {
            log.warn("无法生成重复提交锁Key，跳过防重复检查");
            return joinPoint.proceed();
        }

        // 获取锁过期时间
        int expire = preventDuplicateSubmit.expire();
        RLock rLock = redissonClient.getLock(resubmitLockKey);
        
        try {
            // 尝试获取锁，不等待，过期时间为expire秒
            boolean lockResult = rLock.tryLock(0, expire, TimeUnit.SECONDS);
            
            if (!lockResult) {
                log.warn("检测到重复提交，lockKey: {}", resubmitLockKey);
                throw new CustomException(SystemConstants.REPEAT_SUBMIT_MSG);
            }
            
            log.debug("防重复提交锁获取成功，lockKey: {}", resubmitLockKey);
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取防重复提交锁被中断", e);
            throw new CustomException("系统繁忙，请稍后再试");
        }
    }

    /**
     * 生成重复提交锁的Key
     * 格式: LOCK:RESUBMIT:{jti}:{method}-{uri}
     * 
     * @return 锁Key，如果无法获取则返回null
     */
    private String generateResubmitLockKey() {
        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (StringUtils.isBlank(token) || !token.startsWith(SecurityConstants.JWT_TOKEN_PREFIX)) {
            return null;
        }
        
        try {
            token = token.substring(SecurityConstants.JWT_TOKEN_PREFIX.length());
            Claims claims = tokenService.getTokenClaims(token);
            String jti = claims.getId();
            
            return RESUBMIT_LOCK_PREFIX + jti + ":" + 
                   request.getMethod() + "-" + request.getRequestURI();
        } catch (Exception e) {
            log.error("解析Token失败", e);
            return null;
        }
    }
}
