package com.admin.module.workflow.listener;

import com.admin.module.system.entity.SysUser;
import com.admin.module.system.service.SysUserService;
import com.admin.module.workflow.entity.WorkflowInstance;
import com.admin.module.workflow.entity.WorkflowTask;
import com.admin.module.workflow.mapper.WorkflowInstanceMapper;
import com.admin.module.workflow.mapper.WorkflowTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Flowable 任务监听器
 * 用于监听任务创建、分配、完成等事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowTaskListener implements TaskListener {

    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final SysUserService sysUserService;

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        log.info("任务事件: taskId={}, eventName={}, processInstanceId={}",
                delegateTask.getId(), eventName, delegateTask.getProcessInstanceId());

        switch (eventName) {
            case EVENTNAME_CREATE:
                handleTaskCreate(delegateTask);
                break;
            case EVENTNAME_ASSIGNMENT:
                handleTaskAssignment(delegateTask);
                break;
            case EVENTNAME_COMPLETE:
                handleTaskComplete(delegateTask);
                break;
            case EVENTNAME_DELETE:
                handleTaskDelete(delegateTask);
                break;
            default:
                log.debug("未处理的任务事件: {}", eventName);
        }
    }

    /**
     * 处理任务创建事件
     */
    private void handleTaskCreate(DelegateTask delegateTask) {
        try {
            WorkflowInstance instance = workflowInstanceMapper.selectByProcessInstanceId(delegateTask.getProcessInstanceId());
            if (instance == null) {
                log.warn("流程实例不存在: processInstanceId={}", delegateTask.getProcessInstanceId());
                return;
            }

            WorkflowTask task = new WorkflowTask();
            task.setInstanceId(instance.getId());
            task.setTaskId(delegateTask.getId());
            task.setDefinitionId(instance.getDefinitionId());
            task.setNodeId(delegateTask.getTaskDefinitionKey());
            task.setNodeName(delegateTask.getName());
            task.setTaskType(0);
            task.setArriveTime(LocalDateTime.now());
            task.setStatus(0);
            task.setIsRead(0);
            task.setTenantId(instance.getTenantId());

            if (delegateTask.getAssignee() != null) {
                Long assigneeId = Long.valueOf(delegateTask.getAssignee());
                task.setAssigneeId(assigneeId);

                SysUser user = sysUserService.getById(assigneeId);
                task.setAssigneeName(user != null ? user.getRealName() : "");
            }

            if (delegateTask.getDueDate() != null) {
                task.setDueTime(delegateTask.getDueDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime());
            }

            workflowTaskMapper.insert(task);

            log.info("任务创建成功: taskId={}, instanceId={}", delegateTask.getId(), instance.getId());
        } catch (Exception e) {
            log.error("处理任务创建事件失败", e);
        }
    }

    /**
     * 处理任务分配事件
     */
    private void handleTaskAssignment(DelegateTask delegateTask) {
        try {
            WorkflowTask task = workflowTaskMapper.selectByTaskId(delegateTask.getId());
            if (task == null) {
                return;
            }

            if (delegateTask.getAssignee() != null) {
                Long assigneeId = Long.valueOf(delegateTask.getAssignee());
                task.setAssigneeId(assigneeId);

                SysUser user = sysUserService.getById(assigneeId);
                task.setAssigneeName(user != null ? user.getRealName() : "");

                workflowTaskMapper.updateById(task);

                log.info("任务分配成功: taskId={}, assigneeId={}", delegateTask.getId(), assigneeId);
            }
        } catch (Exception e) {
            log.error("处理任务分配事件失败", e);
        }
    }

    /**
     * 处理任务完成事件
     */
    private void handleTaskComplete(DelegateTask delegateTask) {
        try {
            WorkflowTask task = workflowTaskMapper.selectByTaskId(delegateTask.getId());
            if (task == null) {
                return;
            }

            log.info("任务完成: taskId={}, instanceId={}", delegateTask.getId(), task.getInstanceId());
        } catch (Exception e) {
            log.error("处理任务完成事件失败", e);
        }
    }

    /**
     * 处理任务删除事件
     */
    private void handleTaskDelete(DelegateTask delegateTask) {
        try {
            WorkflowTask task = workflowTaskMapper.selectByTaskId(delegateTask.getId());
            if (task == null) {
                return;
            }

            log.info("任务删除: taskId={}, instanceId={}", delegateTask.getId(), task.getInstanceId());
        } catch (Exception e) {
            log.error("处理任务删除事件失败", e);
        }
    }
}
