package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.module.system.entity.ScheduledJobLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.query.ScheduledJobLogQuery;

/**
* @author sy
* @description 针对表【scheduled_job_log(定时任务日志表)】的数据库操作Service
* @createDate 2024-05-27 15:30:12
*/
public interface ScheduledJobLogService extends IService<ScheduledJobLog> {

    /**
     * 定时任务日志分页列表
     * @param scheduledJobLogQuery
     * @return
     */
    IPage<ScheduledJobLog> selectScheduledJobLogsPage(ScheduledJobLogQuery scheduledJobLogQuery);
}
