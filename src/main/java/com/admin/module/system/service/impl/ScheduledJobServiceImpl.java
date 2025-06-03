package com.admin.module.system.service.impl;

import com.admin.module.system.mapper.ScheduledJobMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.common.enums.StatusEnum;
import com.admin.common.util.QuartzUtils;
import com.admin.module.system.entity.ScheduledJob;
import com.admin.module.system.query.ScheduledJobQuery;
import com.admin.module.system.service.ScheduledJobService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author sy
* @description 针对表【scheduled_job(定时任务表)】的数据库操作Service实现
* @createDate 2024-05-27 15:30:12
*/
@RequiredArgsConstructor
@Service
public class ScheduledJobServiceImpl extends ServiceImpl<ScheduledJobMapper, ScheduledJob> implements ScheduledJobService{


    private final QuartzUtils quartzUtils;

    private final Scheduler scheduler;

    private final ApplicationContext applicationContext;


    /**
     * 项目启动，初始化定时器
     */
    @PostConstruct
    private void initScheduleJob() {
        LambdaQueryWrapper<ScheduledJob> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduledJob::getStatus, StatusEnum.ENABLE.getValue());
        List<ScheduledJob> scheduledJobs = this.list(queryWrapper);
        try {
            // 清理现有调度器任务
            scheduler.clear();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }

        if (CollectionUtils.isEmpty(scheduledJobs)) {
            return;
        }

        // 执行定时任务
        scheduledJobs.forEach(scheduledJob -> {
            CronTrigger cronTrigger = quartzUtils.getCronTrigger(scheduler, scheduledJob.getJobId());
            if (cronTrigger == null) {
                quartzUtils.createJob(scheduledJob);
            } else {
                quartzUtils.updateJob(scheduledJob);
            }

        });
    }


    /**
     * 定时任务分页列表
     *
     * @param scheduledJobQuery
     * @return
     */
    @Override
    public IPage<ScheduledJob> selectScheduledJobPage(ScheduledJobQuery scheduledJobQuery) {
        LambdaQueryWrapper<ScheduledJob> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(scheduledJobQuery.getJobName())) {
            queryWrapper.like(ScheduledJob::getJobName, scheduledJobQuery.getJobName());
        }
        if (scheduledJobQuery.getStatus() != null) {
            queryWrapper.eq(ScheduledJob::getStatus, scheduledJobQuery.getStatus());
        }
        queryWrapper.orderByDesc(ScheduledJob::getCreateTime);
        IPage<ScheduledJob> page = new Page<>(scheduledJobQuery.getPage(), scheduledJobQuery.getLimit());
        IPage<ScheduledJob> iPage = this.page(page, queryWrapper);
        return iPage;
    }

    /**
     * 新增定时任务
     *
     * @param scheduledJob
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveScheduledJob(ScheduledJob scheduledJob) {
        boolean result = this.save(scheduledJob);
        quartzUtils.createJob(scheduledJob);
        return result;
    }


    /**
     * 获取定时任务详情
     *
     * @param jobId
     * @return
     */
    @Override
    public ScheduledJob getScheduledJobDetail(Long jobId) {
        ScheduledJob scheduledJob = this.getById(jobId);
        return scheduledJob;
    }

    /**
     * 更新定时任务
     *
     * @param scheduledJob
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateScheduledJob(ScheduledJob scheduledJob) {
        boolean result = this.updateById(scheduledJob);
        quartzUtils.updateJob(scheduledJob);
        return result;
    }

    /**
     * 删除定时任务
     *
     * @param jobIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteScheduledJob(List<Long> jobIds) {
        jobIds.forEach(quartzUtils::deleteJob);
        return this.removeByIds(jobIds);
    }

    /**
     * 执行定时任务
     *
     * @param jobIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean runJobs(List<Long> jobIds) {
        jobIds.forEach(id -> {
            quartzUtils.resumeJob(id);
        });

        LambdaUpdateWrapper<ScheduledJob> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ScheduledJob::getStatus, StatusEnum.ENABLE.getValue());
        updateWrapper.in(ScheduledJob::getJobId, jobIds);
        return this.update(updateWrapper);
    }

    /**
     * 暂停定时任务
     *
     * @param jobIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean pauseJobs(List<Long> jobIds) {

        jobIds.forEach(id -> {
            quartzUtils.pauseJob(id);
        });

        LambdaUpdateWrapper<ScheduledJob> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ScheduledJob::getStatus, StatusEnum.DISABLE.getValue());
        updateWrapper.in(ScheduledJob::getJobId, jobIds);
        return this.update(updateWrapper);
    }

    /**
     * 获取所有自定义定时任务的beanName
     *
     * @return
     */
    @Override
    public List<String> selectAllJobBeanName() {
        ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        String[] allBeanName = beanFactory.getBeanDefinitionNames();

        // 帅选出定时任务的 beanName
        List<String> jobBeanNameList = Arrays.stream(allBeanName)
                .filter(beanName -> beanFactory.isTypeMatch(beanName, Job.class))
                .collect(Collectors.toList());

        // 将 Bean 名称转换为类的全限定名称
        List<String> fullBeanNameList = jobBeanNameList.stream()
                .map(jobBeanName -> {
                    Class<?> beanClass = applicationContext.getType(jobBeanName);
                    assert beanClass != null;
                    return beanClass.getName();
                })
                .collect(Collectors.toList());

        return fullBeanNameList;
    }
}




