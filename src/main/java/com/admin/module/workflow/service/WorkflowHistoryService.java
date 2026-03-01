package com.admin.module.workflow.service;

import com.admin.module.workflow.entity.WorkflowHistory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 流程历史记录服务接口
 */
public interface WorkflowHistoryService extends IService<WorkflowHistory> {

    /**
     * 获取流程历史记录
     */
    List<Map<String, Object>> getHistoryByInstanceId(Long instanceId);

    /**
     * 获取任务历史记录
     */
    List<Map<String, Object>> getHistoryByTaskId(Long taskId);

    /**
     * 添加历史记录
     */
    void addHistory(Long instanceId, Long taskId, String nodeId, String nodeName,
                    Integer operationType, Long operatorId, String operatorName,
                    String comment, Integer result, Map<String, Object> variables);

    /**
     * 获取流程审批轨迹
     */
    List<Map<String, Object>> getApprovalTrack(Long instanceId);

    /**
     * 获取流程时间轴
     */
    List<Map<String, Object>> getTimeline(Long instanceId);
}
