package com.admin.common.util;

import com.admin.common.enums.StatusEnum;
import com.admin.module.system.entity.ScheduledJob;
import com.admin.module.system.entity.ScheduledJobLog;
import com.admin.module.system.service.ScheduledJobLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
public class ScheduledJobLogUtil {

    public static void saveScheduledJobLog(JobExecutionContext context, LocalDateTime startTime, LocalDateTime endTime) {
        //  从 jobDataMap 中获取 scheduledJob
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String jobId = context.getJobDetail().getKey().getName();
        ScheduledJob scheduledJob = (ScheduledJob) jobDataMap.get(jobId);

        ScheduledJobLogService scheduledJobLogService = SpringContextUtil.getBean(ScheduledJobLogService.class);

        ScheduledJobLog scheduledJobLog = new ScheduledJobLog();

        try {
            log.info("准备执行定时任务, 任务id: {}", scheduledJob.getJobId());
            Duration duration = Duration.between(startTime, endTime);
            long executionMillis = duration.toMillis();


            scheduledJobLog.setEndTime(LocalDateTime.now());
            scheduledJobLog.setJobId(scheduledJob.getJobId());
            scheduledJobLog.setJobName(scheduledJob.getJobName());
            scheduledJobLog.setBeanName(scheduledJob.getJobClass());
            scheduledJobLog.setExecuteTime(executionMillis);
            scheduledJobLog.setStartTime(startTime);
            scheduledJobLog.setEndTime(endTime);
            scheduledJobLog.setStatus(StatusEnum.ENABLE.getValue());
        } catch (Exception e) {
            scheduledJobLog.setStatus(StatusEnum.DISABLE.getValue());
            scheduledJobLog.setErrorInfo(StringUtils.substring(e.getMessage(), 0, 2000));
            throw new RuntimeException(e);
        } finally {
            // 新增定时任务日志
            scheduledJobLogService.save(scheduledJobLog);
        }
    }
}
