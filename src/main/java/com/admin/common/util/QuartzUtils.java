package com.admin.common.util;

import com.admin.module.system.entity.ScheduledJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

/**
 * 封装定时任务类的新增、修改、删除、立即执行一次、暂停等通用方法
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class QuartzUtils {

    private final Scheduler scheduler;


    /**
     * 获取 jobKey
     * @param jobId
     * @return
     */
    private static JobKey getJobKey(Long jobId) {
        return JobKey.jobKey(jobId.toString());
    }

    /**
     * 获取 triggerKey
     * @param jobId
     * @return
     */
    private static TriggerKey getTriggerKey(Long jobId) {
        return TriggerKey.triggerKey(jobId.toString());
    }


    /**
     * 获取 cron 表达式触发器
     * @param scheduler
     * @param jobId
     * @return
     */
    public  CronTrigger getCronTrigger(Scheduler scheduler, Long jobId) {
        try {
            return (CronTrigger) scheduler.getTrigger(getTriggerKey(jobId));
        } catch (SchedulerException e) {
            log.error("获取定时任务Crontrigger出现异常：{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }




    /**
     * 新增定时任务
     * @param scheduledJob
     */
    public  void createJob(ScheduledJob scheduledJob) {
        try {
            // 获取到定时任务类的执行类
            Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(scheduledJob.getJobClass());

            // 构建定时任务信息
            JobKey jobKey = getJobKey(scheduledJob.getJobId());
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobKey) // 定时任务类唯一标识
                    .usingJobData(new JobDataMap())
                    .build();

            // 将 scheduledJob 对象存储到 jobDataMap
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put(String.valueOf(scheduledJob.getJobId()), scheduledJob);

            // 定时任务类参数 (向 JobDataMap 添加参数)
//            if (StringUtils.isNotEmpty(scheduledJob.getParams())) {
//                ObjectMapper objectMapper = new ObjectMapper();
//                Map<String, Object> map = objectMapper.readValue(scheduledJob.getParams(), HashMap.class);
//                jobDetail.getJobDataMap().putAll(map);
//            }

            // 构建定时任务执行方式
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(scheduledJob.getCronExpression());

            // 构造触发器trigger
            TriggerKey triggerKey = getTriggerKey(scheduledJob.getJobId());
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)  // 触发器唯一标识
                    .withSchedule(cronScheduleBuilder)
                    .build();


            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            log.error("创建定时任务出错：{}", e.getMessage());
            throw new RuntimeException("创建定时任务出错：", e);
        }
    }


    /**
     * 更新定时任务 (即修改 cron表达式)
     * @param scheduledJob
     */
    public void updateJob(ScheduledJob scheduledJob) {
        try {
            // 获取对应任务的触发器
            TriggerKey triggerKey = getTriggerKey(scheduledJob.getJobId());
            // 设置定时任务执行方式
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduledJob.getCronExpression());

            // 重新构建定时任务的触发器trigger
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            trigger = trigger.getTriggerBuilder()
                    .withIdentity(triggerKey)
                    .withSchedule(scheduleBuilder)
                    .build();

            // 重新构建触发器
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (Exception e) {
            log.error("更新定时任务出错：{}", e.getMessage());
            throw new RuntimeException("更新定时任务出错：", e);
        }
    }


    /**
     * 删除定时任务
     * @param jobId
     */
    public void deleteJob(Long jobId) {
        try {
            JobKey jobKey = getJobKey(jobId);
            scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            log.error("删除定时任务出错：{}", e.getMessage());
            throw new RuntimeException("删除定时任务出错：", e);
        }
    }


    /**
     * 执行定时任务 (立即执行一次)
     * @param jobId
     */
    public void runJobOnce(Long jobId) {
        try {
            JobKey jobKey = getJobKey(jobId);
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            log.error("立即执行一次定时任务出错：{}", e.getMessage());
            throw new RuntimeException("立即执行一次定时任务出错：", e);
        }
    }


    /**
     * 暂停定时任务
     * @param jobId
     */
    public void pauseJob(Long jobId) {
        try {
            JobKey jobKey = getJobKey(jobId);
            scheduler.pauseJob(jobKey);
        } catch (Exception e) {
            log.error("暂停定时任务出错：{}", e.getMessage());
            throw new RuntimeException("暂停定时任务出错：", e);
        }
    }


    /**
     * 启动定时任务
     * @param jobId
     */
    public void resumeJob(Long jobId) {
        try {
            JobKey jobKey = getJobKey(jobId);
            scheduler.resumeJob(jobKey);
        } catch (Exception e) {
            log.error("启动定时任务出错：{}", e.getMessage());
            throw new RuntimeException("启动定时任务出错：", e);
        }
    }



}
