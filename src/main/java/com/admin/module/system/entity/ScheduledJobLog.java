package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务日志表
 * @TableName scheduled_job_log
 */
@TableName(value ="scheduled_job_log")
@Data
public class ScheduledJobLog implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 定时任务id
     */
    private Long jobId;

    /**
     * 定时任务名称
     */
    private String jobName;

    /**
     * 调用目标类  (执行的定时任务类)
     */
    private String beanName;

    /**
     * 调用目标方法名
     */
    private String methodName;

    /**
     * 失败信息
     */
    private String errorInfo;

    /**
     * 执行状态 (1: 正常，-1：失败)
     */
    private Integer status;

    /**
     * 执行消耗时间
     */
    private Long executeTime;

    /**
     * 开始执行时间
     */
    private LocalDateTime startTime;

    /**
     * 结束执行时间
     */
    private LocalDateTime endTime;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 更新人
     */
    private Long updateUser;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}