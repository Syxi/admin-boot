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
 * 处理重复提交的切面
 *
 * @Author: suYan
 * @Date: 2023-12-07
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RepeatSubmitAspect {

    private final RedissonClient redissonClient;

    public final TokenService tokenService;

    public static final String RESUBMIT_LOCK_PREFIX =  "LOCK:RESUBMIT:";

    /**
     * 防重复提交切点
     * @param noRepeatSubmit
     */
    @Pointcut("@annotation(noRepeatSubmit)")
    public void preventDuplicateSubmitPointCut(NoRepeatSubmit noRepeatSubmit) {
        log.info("防止重复提交切点");
    }

    @Around("preventDuplicateSubmitPointCut(preventDuplicateSubmit)")
    public Object doAround(ProceedingJoinPoint joinPoint, NoRepeatSubmit preventDuplicateSubmit) throws Throwable {
        String resubmitLocKey = generateResubmitLockKey();
        if (resubmitLocKey != null) {
            // 防重复提交锁过期时间
            int expire = preventDuplicateSubmit.expire();
            RLock rLock = redissonClient.getLock(resubmitLocKey);
            // 获取锁失败，直接返回 false
            boolean lockResult = rLock.tryLock(0, expire, TimeUnit.SECONDS);
            if (!lockResult) {
                throw new CustomException(SystemConstants.REPEAT_SUBMIT_MSG);
            }
        }

        return joinPoint.proceed();
    }


    /**
     * 获取重复提交的锁的 Key
     * @return
     */
    private String generateResubmitLockKey() {
        String resubmitLockKey = null;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotBlank(token) && token.startsWith(SecurityConstants.JWT_TOKEN_PREFIX)) {
            token = token.substring(SecurityConstants.JWT_TOKEN_PREFIX.length());
            Claims claims = tokenService.getTokenClaims(token);
            String jti = claims.getId();
            resubmitLockKey = RESUBMIT_LOCK_PREFIX + jti + ":" + request.getMethod() + "-" + request.getRequestURI();
        }
        return resubmitLockKey;
    }


}
