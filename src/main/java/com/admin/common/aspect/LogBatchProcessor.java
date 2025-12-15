package com.admin.common.aspect;

import com.admin.common.context.BaseServiceBeanContext;
import com.admin.module.system.entity.UserLoginLog;
import com.admin.module.system.entity.UserOperationLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 日志批处理处理器
 * 用于异步批量处理操作日志和登录日志，减少数据库交互次数
 */
@Slf4j
@Component
public class LogBatchProcessor {
    

    // 操作日志队列
    private final Queue<UserOperationLog> operationLogQueue = new ConcurrentLinkedQueue<>();
    
    // 登录日志队列
    private final Queue<UserLoginLog> loginLogQueue = new ConcurrentLinkedQueue<>();
    
    // 定时调度器
    private ScheduledExecutorService scheduler;
    
    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        // 初始化定时调度器
        scheduler = Executors.newScheduledThreadPool(2);
        
        // 定时批量保存操作日志（每5秒或队列达到100条时触发）
        scheduler.scheduleAtFixedRate(this::batchSaveOperationLogs, 0, 5, TimeUnit.SECONDS);
        
        // 定时批量保存登录日志（每5秒或队列达到50条时触发）
        scheduler.scheduleAtFixedRate(this::batchSaveLoginLogs, 0, 5, TimeUnit.SECONDS);
    }
    
    /**
     * 添加操作日志到队列
     * @param log 操作日志
     */
    public void addOperationLog(UserOperationLog userOperationLog) {
        if (userOperationLog == null) {
            log.warn("尝试添加空的操作日志");
            return;
        }
        
        operationLogQueue.offer(userOperationLog);
        // 如果队列达到100条，立即触发批量保存
        if (operationLogQueue.size() >= 100) {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.execute(this::batchSaveOperationLogs);
            } else {
                log.warn("调度器未初始化或已关闭，无法执行操作日志批量保存");
            }
        }
    }
    
    /**
     * 添加登录日志到队列
     * @param log 登录日志
     */
    public void addLoginLog(UserLoginLog userLoginLog) {
        if (userLoginLog == null) {
            log.warn("尝试添加空的登录日志");
            return;
        }
        
        loginLogQueue.offer(userLoginLog);
        // 如果队列达到50条，立即触发批量保存
        if (loginLogQueue.size() >= 50) {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.execute(this::batchSaveLoginLogs);
            } else {
                log.warn("调度器未初始化或已关闭，无法执行登录日志批量保存");
            }
        }
    }
    
    /**
     * 批量保存操作日志
     */
    private void batchSaveOperationLogs() {
        List<UserOperationLog> logs = new ArrayList<>();
        int batchSize = Math.min(operationLogQueue.size(), 100);
        
        // 从队列中取出最多100条日志
        for (int i = 0; i < batchSize; i++) {
            UserOperationLog log = operationLogQueue.poll();
            if (log != null) {
                logs.add(log);
            }
        }
        
        // 批量保存
        if (!logs.isEmpty()) {
            try {
                if (BaseServiceBeanContext.userOperationLogService != null) {
                    BaseServiceBeanContext.userOperationLogService.saveBatch(logs);
                    log.debug("批量保存操作日志 {} 条", logs.size());
                } else {
                    log.warn("UserOperationLogService 未初始化");
                }
            } catch (Exception e) {
                log.error("批量保存操作日志失败", e);
                // 保存失败时将日志重新放入队列
                logs.forEach(operationLogQueue::offer);
            }
        }
    }
    
    /**
     * 批量保存登录日志
     */
    private void batchSaveLoginLogs() {
        List<UserLoginLog> logs = new ArrayList<>();
        int batchSize = Math.min(loginLogQueue.size(), 50);
        
        // 从队列中取出最多50条日志
        for (int i = 0; i < batchSize; i++) {
            UserLoginLog log = loginLogQueue.poll();
            if (log != null) {
                logs.add(log);
            }
        }
        
        // 批量保存
        if (!logs.isEmpty()) {
            try {
                if (BaseServiceBeanContext.userLoginLogService != null) {
                    BaseServiceBeanContext.userLoginLogService.saveBatch(logs);
                    log.debug("批量保存登录日志 {} 条", logs.size());
                } else {
                    log.warn("UserLoginLogService 未初始化");
                }
            } catch (Exception e) {
                log.error("批量保存登录日志失败", e);
                // 保存失败时将日志重新放入队列
                logs.forEach(loginLogQueue::offer);
            }
        }
    }
}