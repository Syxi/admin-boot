package com.admin.module.system.service.impl;

import com.admin.module.system.service.ScheduledJobLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.module.system.mapper.ScheduledJobLogMapper;
import com.admin.module.system.entity.ScheduledJobLog;
import com.admin.module.system.query.ScheduledJobLogQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author sy
* @description 针对表【scheduled_job_log(定时任务日志表)】的数据库操作Service实现
* @createDate 2024-05-27 15:30:12
*/
@Service
public class ScheduledJobLogServiceImpl extends ServiceImpl<ScheduledJobLogMapper, ScheduledJobLog>
    implements ScheduledJobLogService {

    /**
     * 定时任务日志分页列表
     *
     * @param scheduledJobLogQuery
     * @return
     */
    @Override
    public IPage<ScheduledJobLog> selectScheduledJobLogsPage(ScheduledJobLogQuery scheduledJobLogQuery) {
        LambdaQueryWrapper<ScheduledJobLog> queryWrapper = new LambdaQueryWrapper<ScheduledJobLog>();
        if (StringUtils.isNotBlank(scheduledJobLogQuery.getJobName())) {
            queryWrapper.like(ScheduledJobLog::getJobName, scheduledJobLogQuery.getJobName());
        }
        if (StringUtils.isNotBlank(scheduledJobLogQuery.getBeanName())) {
            queryWrapper.like(ScheduledJobLog::getBeanName, scheduledJobLogQuery.getBeanName());
        }
        if (scheduledJobLogQuery.getStatus() != null) {
            queryWrapper.eq(ScheduledJobLog::getStatus, scheduledJobLogQuery.getStatus());
        }
        queryWrapper.orderByDesc(ScheduledJobLog::getStartTime);
        IPage<ScheduledJobLog> page = new Page<>(scheduledJobLogQuery.getPage(), scheduledJobLogQuery.getLimit());
        IPage<ScheduledJobLog> scheduledJobLogIPage = this.page(page, queryWrapper);
        return scheduledJobLogIPage;
    }
}




