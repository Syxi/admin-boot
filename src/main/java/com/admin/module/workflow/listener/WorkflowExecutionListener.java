package com.admin.module.workflow.listener;

import com.admin.module.workflow.entity.WorkflowHistory;
import com.admin.module.workflow.entity.WorkflowInstance;
import com.admin.module.workflow.mapper.WorkflowHistoryMapper;
import com.admin.module.workflow.mapper.WorkflowInstanceMapper;
import com.admin.module.workflow.service.WorkflowCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Flowable 执行监听器
 * 用于监听流程开始、结束、节点进入等事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowExecutionListener implements ExecutionListener {

    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowHistoryMapper workflowHistoryMapper;
    private final WorkflowCallbackService workflowCallbackService;

    @Override
    public void notify(DelegateExecution execution) {
        String eventName = execution.getEventName();
        String processInstanceId = execution.getProcessInstanceId();
        String activityId = execution.getCurrentActivityId();
        String activityName = execution.getCurrentActivityName();

        log.info("流程执行事件: eventName={}, processInstanceId={}, activityId={}, activityName={}",
                eventName, processInstanceId, activityId, activityName);

        switch (eventName) {
            case EVENTNAME_START:
                handleProcessStart(execution);
                break;
            case EVENTNAME_END:
                handleProcessEnd(execution);
                break;
            case EVENTNAME_TAKE:
                handleTransition(execution);
                break;
            default:
                log.debug("未处理的执行事件: {}", eventName);
        }
    }

    /**
     * 处理流程开始事件
     */
    private void handleProcessStart(DelegateExecution execution) {
        try {
            WorkflowInstance instance = workflowInstanceMapper.selectByProcessInstanceId(execution.getProcessInstanceId());
            if (instance != null) {
                log.info("流程开始: instanceId={}, processInstanceId={}", instance.getId(), execution.getProcessInstanceId());

                Map<String, Object> data = new HashMap<>();
                data.put("processInstanceId", execution.getProcessInstanceId());
                data.put("startTime", instance.getStartTime());
                data.put("applicantId", instance.getApplicantId());
                data.put("applicantName", instance.getApplicantName());

                workflowCallbackService.onProcessStart(instance.getId(), data);
            }
        } catch (Exception e) {
            log.error("处理流程开始事件失败", e);
        }
    }

    /**
     * 处理流程结束事件
     */
    private void handleProcessEnd(DelegateExecution execution) {
        try {
            WorkflowInstance instance = workflowInstanceMapper.selectByProcessInstanceId(execution.getProcessInstanceId());
            if (instance == null) {
                return;
            }

            if (instance.getStatus() != 1) {
                instance.setStatus(1);
                instance.setEndTime(LocalDateTime.now());

                if (instance.getStartTime() != null && instance.getEndTime() != null) {
                    instance.setDuration(java.time.Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis());
                }

                if (instance.getResult() == null || instance.getResult() == 0) {
                    instance.setResult(1);
                }

                workflowInstanceMapper.updateById(instance);

                log.info("流程结束: instanceId={}, processInstanceId={}", instance.getId(), execution.getProcessInstanceId());

                Map<String, Object> data = new HashMap<>();
                data.put("processInstanceId", execution.getProcessInstanceId());
                data.put("endTime", instance.getEndTime());
                data.put("duration", instance.getDuration());
                data.put("result", instance.getResult());

                workflowCallbackService.onProcessEnd(instance.getId(), data);
            }
        } catch (Exception e) {
            log.error("处理流程结束事件失败", e);
        }
    }

    /**
     * 处理流转事件
     */
    private void handleTransition(DelegateExecution execution) {
        try {
            String activityId = execution.getCurrentActivityId();
            String activityName = execution.getCurrentActivityName();

            log.debug("流程流转: processInstanceId={}, from={}, to={}",
                    execution.getProcessInstanceId(), activityId, activityName);
        } catch (Exception e) {
            log.error("处理流转事件失败", e);
        }
    }
}
