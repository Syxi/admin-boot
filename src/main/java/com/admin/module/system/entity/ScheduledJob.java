package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务表
 * @TableName scheduled_job
 */
@TableName(value ="scheduled_job")
@Data
public class ScheduledJob extends BaseEntity {
    /**
     * 
     */
    @TableId
    private Long jobId;

    /**
     * 租户ID
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long tenantId;

    /**
     * 定时任务名称
     */
    private String jobName;

    /**
     * 任务执行类  (执行的定时任务类)
     */
    private String jobClass;


    /**
     * 定时任务类参数
     */
    private String params;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 是否开启定时任务 （1：开启，-1：暂停）
     */
    private Integer status;

    /**
     * 定时任务最近一次执行的时间
     */
    private LocalDateTime lastExecutedTime;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}