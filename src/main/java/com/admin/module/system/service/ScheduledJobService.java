package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.module.system.entity.ScheduledJob;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.query.ScheduledJobQuery;

import java.util.List;

/**
* @author sy
* @description 针对表【scheduled_job(定时任务表)】的数据库操作Service
* @createDate 2024-05-27 15:30:12
*/
public interface ScheduledJobService extends IService<ScheduledJob> {

    /**
     * 定时任务分页列表
     * @param scheduledJobQuery
     * @return
     */
    IPage<ScheduledJob> selectScheduledJobPage(ScheduledJobQuery scheduledJobQuery);

    /**
     * 新增定时任务
     * @param scheduledJob
     * @return
     */
    boolean saveScheduledJob(ScheduledJob scheduledJob);

    /**
     * 获取定时任务详情
     * @param jobId
     * @return
     */
    ScheduledJob getScheduledJobDetail(Long jobId);

    /**
     * 更新定时任务
     * @param scheduledJob
     * @return
     */
    boolean updateScheduledJob(ScheduledJob scheduledJob);

    /**
     * 删除定时任务
     * @param jobIds
     * @return
     */
    boolean deleteScheduledJob(List<Long> jobIds);

    /**
     * 执行定时任务
     * @param jobIds
     * @return
     */
    boolean runJobs(List<Long> jobIds);

    /**
     * 暂停定时任务
     * @param jobIds
     * @return
     */
    boolean pauseJobs(List<Long> jobIds);


    /**
     * 获取所有自定义定时任务的beanName
     * @return
     */
    List<String> selectAllJobBeanName();
}
