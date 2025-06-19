package com.admin.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.ScheduledJob;
import com.admin.module.system.query.ScheduledJobQuery;
import com.admin.module.system.service.ScheduledJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "定时任务类")
@RequestMapping("/scheduledJob")
@RequiredArgsConstructor
@RestController
public class ScheduledJobController {


    private final ScheduledJobService scheduledJobService;

    @Operation(summary = "定时任务分页列表")
    @GetMapping("/page")
    public PageResult<ScheduledJob> selectScheduledJobPage(ScheduledJobQuery scheduledJobQuery) {
        IPage<ScheduledJob> scheduledJobIPage = scheduledJobService.selectScheduledJobPage(scheduledJobQuery);
        return PageResult.success(scheduledJobIPage);
    }


    @Operation(summary = "新增定时任务")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:scheduledJob:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> saveScheduledJob(@RequestBody ScheduledJob scheduledJob) {
        boolean result = scheduledJobService.saveScheduledJob(scheduledJob);
        return ResultVO.judge(result);
    }



    @Operation(summary = "获取定时任务信息")
    @GetMapping("/scheduledJobDetail/{id}")
    public ResultVO<ScheduledJob> getScheduledJobDetail(@PathVariable("id") Long id) {
        ScheduledJob scheduledJob = scheduledJobService.getScheduledJobDetail(id);
        return ResultVO.success(scheduledJob);
    }


    @Operation(summary = "更新定时任务")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:scheduledJob:edit')")
    @PutMapping("/update")
    public ResultVO<Boolean> updateScheduledJob(@RequestBody ScheduledJob scheduledJob) {
        boolean result = scheduledJobService.updateScheduledJob(scheduledJob);
        return ResultVO.judge(result);
    }


    @Operation(summary = "删除定时任务")
    @PreAuthorize("@pms.hasPerm('sys:scheduledJob:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteScheduledJob(@RequestBody List<Long> ids) {
        boolean result = scheduledJobService.deleteScheduledJob(ids);
        return ResultVO.judge(result);
    }


    @Operation(summary = "执行定时任务")
    @PreAuthorize("@pms.hasPerm('sys:scheduledJob:run')")
    @PostMapping("/run")
    public ResultVO<Boolean> runJobs(@RequestBody List<Long> ids) {
        boolean result = scheduledJobService.runJobs(ids);
        return ResultVO.judge(result);
    }


    @Operation(summary = "暂停定时任务")
    @PreAuthorize("@pms.hasPerm('sys:scheduleJob:pause')")
    @PostMapping("/pause")
    public ResultVO<Boolean> pauseJobs(@RequestBody List<Long> ids) {
        boolean result = scheduledJobService.pauseJobs(ids);
        return ResultVO.judge(result);
    }


    @Operation(summary = "获取所有自定义定时任务的beanName")
    @GetMapping("/jobBean")
    public ResultVO<List<String>> selectAllJobBeanName() {
        List<String> jobBeanNames = scheduledJobService.selectAllJobBeanName();
        return ResultVO.success(jobBeanNames);
    }




}
