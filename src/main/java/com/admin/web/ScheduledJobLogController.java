package com.admin.web;

import com.admin.common.result.PageResult;
import com.admin.module.system.entity.ScheduledJobLog;
import com.admin.module.system.query.ScheduledJobLogQuery;
import com.admin.module.system.service.ScheduledJobLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "定时任务日志")
@RestController
@RequestMapping("/api/scheduledJobLog")
@RequiredArgsConstructor
public class ScheduledJobLogController {

    private final ScheduledJobLogService scheduledJobLogService;

    @Operation(summary = "定时任务日志分页列表")
    @GetMapping("/page")
    public PageResult<ScheduledJobLog> selectScheduledJobLogsPage(ScheduledJobLogQuery scheduledJobLogQuery) {
        IPage<ScheduledJobLog> scheduledJobLogIPage = scheduledJobLogService.selectScheduledJobLogsPage(scheduledJobLogQuery);
        return PageResult.success(scheduledJobLogIPage);
    }
}
