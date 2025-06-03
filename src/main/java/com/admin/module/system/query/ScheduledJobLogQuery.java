package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import lombok.Data;

@Data
public class ScheduledJobLogQuery extends BasePage {

    private String jobName;

    private String beanName;

    private Integer status;
}
