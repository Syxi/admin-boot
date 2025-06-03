package com.admin.common.task;

import com.admin.common.util.ScheduledJobLogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 当一个JobDetail（任务定义）与Trigger（触发器）匹配，触发任务执行时，
 * Quartz会调用这个类的executeInternal方法来执行自定义的业务逻辑
 */
@Slf4j
@RequiredArgsConstructor
@Component()
public class RefreshRolePermsCacheTask extends QuartzJobBean {


    /**
     * 自定义任务类的执行逻辑方法
     * @param context
     * @throws JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 定时刷新角色权限缓存
        LocalDateTime startTime = LocalDateTime.now();
        // 任务执行总时长 毫秒
        LocalDateTime endTime = LocalDateTime.now();
        log.info("refreshRolePermsCache定时任务...");
        // 定时任务日志
        ScheduledJobLogUtil.saveScheduledJobLog(context, startTime, endTime);

    }
}
