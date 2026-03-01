package com.admin.module.workflow.service;

import com.admin.module.workflow.entity.WorkflowCallback;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 流程回调服务接口
 */
public interface WorkflowCallbackService extends IService<WorkflowCallback> {

    /**
     * 创建回调记录
     */
    void createCallback(Long instanceId, String businessType, String businessKey,
                       Integer callbackType, String callbackUrl, Map<String, Object> callbackData);

    /**
     * 发送回调通知
     */
    void sendCallback(Long callbackId);

    /**
     * 处理流程开始回调
     */
    void onProcessStart(Long instanceId, Map<String, Object> data);

    /**
     * 处理流程结束回调
     */
    void onProcessEnd(Long instanceId, Map<String, Object> data);

    /**
     * 处理节点进入回调
     */
    void onNodeEnter(Long instanceId, String nodeId, Map<String, Object> data);

    /**
     * 处理节点完成回调
     */
    void onNodeComplete(Long instanceId, String nodeId, Map<String, Object> data);

    /**
     * 处理任务分配回调
     */
    void onTaskAssign(Long instanceId, Long taskId, Map<String, Object> data);

    /**
     * 处理审批结果回调
     */
    void onApprovalResult(Long instanceId, Long taskId, Integer result, Map<String, Object> data);

    /**
     * 重试失败的回调
     */
    void retryFailedCallbacks();
}
