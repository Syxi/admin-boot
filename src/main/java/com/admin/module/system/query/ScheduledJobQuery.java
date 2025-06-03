package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务查询对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduledJobQuery extends BasePage {

    // 任务名称
    private String jobName;

    // 任务状态
    private Integer status;
}
