package com.admin.module.workflow.service.impl;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.entity.SysUser;
import com.admin.module.system.service.SysUserService;
import com.admin.module.workflow.entity.WorkflowInstance;
import com.admin.module.workflow.entity.WorkflowTask;
import com.admin.module.workflow.form.TaskCompleteForm;
import com.admin.module.workflow.form.TaskDelegateForm;
import com.admin.module.workflow.form.TaskTransferForm;
import com.admin.module.workflow.mapper.WorkflowInstanceMapper;
import com.admin.module.workflow.mapper.WorkflowTaskMapper;
import com.admin.module.workflow.query.WorkflowTaskQuery;
import com.admin.module.workflow.service.*;
import com.admin.module.workflow.vo.WorkflowTaskVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowTaskServiceImpl extends ServiceImpl<WorkflowTaskMapper, WorkflowTask>
        implements WorkflowTaskService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowHistoryService workflowHistoryService;
    private final WorkflowCallbackService workflowCallbackService;
    private final SysUserService sysUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResult<WorkflowTaskVO> selectTodoPage(WorkflowTaskQuery query) {
        Page<WorkflowTask> page = new Page<>(query.getPage(), query.getLimit());

        Long userId = query.getAssigneeId() != null ? query.getAssigneeId() : SecurityUtils.getUserId();

        List<String> candidateGroups = getUserCandidateGroups(userId);

        IPage<WorkflowTask> result = workflowTaskMapper.selectTodoPage(page, userId, candidateGroups,
                query.getProcessName(), query.getBusinessTitle());

        IPage<WorkflowTaskVO> voPage = result.convert(this::convertToVO);

        return PageResult.success(voPage);
    }

    @Override
    public PageResult<WorkflowTaskVO> selectDonePage(WorkflowTaskQuery query) {
        Page<WorkflowTask> page = new Page<>(query.getPage(), query.getLimit());

        Long userId = query.getAssigneeId() != null ? query.getAssigneeId() : SecurityUtils.getUserId();

        IPage<WorkflowTask> result = workflowTaskMapper.selectDonePage(page, userId,
                query.getProcessName(), query.getBusinessTitle());

        IPage<WorkflowTaskVO> voPage = result.convert(this::convertToVO);

        return PageResult.success(voPage);
    }

    @Override
    public WorkflowTaskVO getTaskById(Long id) {
        WorkflowTask task = this.getById(id);
        if (task == null) {
            return null;
        }
        return convertToVO(task);
    }

    @Override
    public WorkflowTask getByFlowableTaskId(String taskId) {
        return workflowTaskMapper.selectByTaskId(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> completeTask(TaskCompleteForm form) {
        WorkflowTask workflowTask = this.getById(form.getTaskId());
        if (workflowTask == null) {
            return ResultVO.error("任务不存在");
        }

        if (workflowTask.getStatus() != 0) {
            return ResultVO.error("任务已处理");
        }

        try {
            Long currentUserId = SecurityUtils.getUserId();
            SysUser user = sysUserService.getById(currentUserId);

            Task task = taskService.createTaskQuery()
                    .taskId(workflowTask.getTaskId())
                    .singleResult();

            if (task == null) {
                return ResultVO.error("Flowable任务不存在");
            }

            Map<String, Object> variables = new HashMap<>();
            if (form.getVariables() != null) {
                variables.putAll(form.getVariables());
            }
            variables.put("approved", true);
            variables.put("comment", form.getComment());

            taskService.setVariablesLocal(task.getId(), variables);
            taskService.complete(task.getId(), variables);

            LocalDateTime now = LocalDateTime.now();
            long duration = Duration.between(workflowTask.getArriveTime(), now).toMillis();

            workflowTask.setStatus(1);
            workflowTask.setResult(1);
            workflowTask.setComment(form.getComment());
            workflowTask.setHandleTime(now);
            workflowTask.setDuration(duration);
            this.updateById(workflowTask);

            workflowHistoryService.addHistory(workflowTask.getInstanceId(), workflowTask.getId(),
                    workflowTask.getNodeId(), workflowTask.getNodeName(),
                    2, currentUserId, user != null ? user.getRealName() : "",
                    form.getComment(), 1, variables);

            workflowCallbackService.onApprovalResult(workflowTask.getInstanceId(), workflowTask.getId(), 1, variables);

            updateInstanceCurrentNode(workflowTask.getInstanceId());

            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("完成任务失败", e);
            return ResultVO.error("完成任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> rejectTask(TaskCompleteForm form) {
        WorkflowTask workflowTask = this.getById(form.getTaskId());
        if (workflowTask == null) {
            return ResultVO.error("任务不存在");
        }

        if (workflowTask.getStatus() != 0) {
            return ResultVO.error("任务已处理");
        }

        try {
            Long currentUserId = SecurityUtils.getUserId();
            SysUser user = sysUserService.getById(currentUserId);

            Task task = taskService.createTaskQuery()
                    .taskId(workflowTask.getTaskId())
                    .singleResult();

            if (task == null) {
                return ResultVO.error("Flowable任务不存在");
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("approved", false);
            variables.put("comment", form.getComment());

            runtimeService.setVariable(task.getProcessInstanceId(), "rejected", true);
            runtimeService.setVariable(task.getProcessInstanceId(), "rejectReason", form.getComment());

            taskService.complete(task.getId(), variables);

            LocalDateTime now = LocalDateTime.now();
            long duration = Duration.between(workflowTask.getArriveTime(), now).toMillis();

            workflowTask.setStatus(1);
            workflowTask.setResult(2);
            workflowTask.setComment(form.getComment());
            workflowTask.setHandleTime(now);
            workflowTask.setDuration(duration);
            this.updateById(workflowTask);

            workflowHistoryService.addHistory(workflowTask.getInstanceId(), workflowTask.getId(),
                    workflowTask.getNodeId(), workflowTask.getNodeName(),
                    3, currentUserId, user != null ? user.getRealName() : "",
                    form.getComment(), 2, variables);

            workflowCallbackService.onApprovalResult(workflowTask.getInstanceId(), workflowTask.getId(), 2, variables);

            WorkflowInstance instance = workflowInstanceMapper.selectById(workflowTask.getInstanceId());
            if (instance != null) {
                instance.setResult(2);
                instance.setEndTime(now);
                instance.setDuration(Duration.between(instance.getStartTime(), now).toMillis());
                workflowInstanceMapper.updateById(instance);
            }

            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("驳回任务失败", e);
            return ResultVO.error("驳回任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> transferTask(TaskTransferForm form) {
        WorkflowTask workflowTask = this.getById(form.getTaskId());
        if (workflowTask == null) {
            return ResultVO.error("任务不存在");
        }

        try {
            Long currentUserId = SecurityUtils.getUserId();
            SysUser user = sysUserService.getById(currentUserId);
            SysUser targetUser = sysUserService.getById(form.getTargetUserId());

            Task task = taskService.createTaskQuery()
                    .taskId(workflowTask.getTaskId())
                    .singleResult();

            if (task == null) {
                return ResultVO.error("Flowable任务不存在");
            }

            taskService.setAssignee(task.getId(), String.valueOf(form.getTargetUserId()));

            LocalDateTime now = LocalDateTime.now();
            long duration = Duration.between(workflowTask.getArriveTime(), now).toMillis();

            workflowTask.setStatus(1);
            workflowTask.setResult(3);
            workflowTask.setComment("转办给: " + (targetUser != null ? targetUser.getRealName() : ""));
            workflowTask.setHandleTime(now);
            workflowTask.setDuration(duration);
            this.updateById(workflowTask);

            WorkflowTask newTask = new WorkflowTask();
            newTask.setInstanceId(workflowTask.getInstanceId());
            newTask.setTaskId(workflowTask.getTaskId());
            newTask.setDefinitionId(workflowTask.getDefinitionId());
            newTask.setNodeId(workflowTask.getNodeId());
            newTask.setNodeName(workflowTask.getNodeName());
            newTask.setTaskType(workflowTask.getTaskType());
            newTask.setAssigneeId(form.getTargetUserId());
            newTask.setAssigneeName(targetUser != null ? targetUser.getRealName() : "");
            newTask.setStatus(0);
            newTask.setArriveTime(now);
            newTask.setParentTaskId(workflowTask.getId());
            newTask.setTenantId(workflowTask.getTenantId());
            this.save(newTask);

            workflowHistoryService.addHistory(workflowTask.getInstanceId(), workflowTask.getId(),
                    workflowTask.getNodeId(), workflowTask.getNodeName(),
                    4, currentUserId, user != null ? user.getRealName() : "",
                    form.getReason(), 3, Map.of("targetUserId", form.getTargetUserId()));

            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("转办任务失败", e);
            return ResultVO.error("转办任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> delegateTask(TaskDelegateForm form) {
        WorkflowTask workflowTask = this.getById(form.getTaskId());
        if (workflowTask == null) {
            return ResultVO.error("任务不存在");
        }

        try {
            Long currentUserId = SecurityUtils.getUserId();
            SysUser user = sysUserService.getById(currentUserId);
            SysUser delegateUser = sysUserService.getById(form.getDelegateUserId());

            Task task = taskService.createTaskQuery()
                    .taskId(workflowTask.getTaskId())
                    .singleResult();

            if (task == null) {
                return ResultVO.error("Flowable任务不存在");
            }

            taskService.delegateTask(task.getId(), String.valueOf(form.getDelegateUserId()));

            LocalDateTime now = LocalDateTime.now();

            workflowTask.setStatus(3);
            workflowTask.setResult(4);
            workflowTask.setComment("委派给: " + (delegateUser != null ? delegateUser.getRealName() : ""));
            workflowTask.setHandleTime(now);
            this.updateById(workflowTask);

            workflowHistoryService.addHistory(workflowTask.getInstanceId(), workflowTask.getId(),
                    workflowTask.getNodeId(), workflowTask.getNodeName(),
                    5, currentUserId, user != null ? user.getRealName() : "",
                    form.getReason(), 4, Map.of("delegateUserId", form.getDelegateUserId()));

            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("委派任务失败", e);
            return ResultVO.error("委派任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> revokeTask(Long taskId, String reason) {
        return ResultVO.success(true);
    }

    @Override
    public Map<String, Object> getTaskFormData(Long taskId) {
        WorkflowTask task = this.getById(taskId);
        if (task == null || task.getFormData() == null) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(task.getFormData(), new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败", e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> saveTaskFormData(Long taskId, Map<String, Object> formData) {
        WorkflowTask task = this.getById(taskId);
        if (task == null) {
            return ResultVO.error("任务不存在");
        }

        try {
            task.setFormData(objectMapper.writeValueAsString(formData));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return ResultVO.error("保存失败");
        }
        boolean success = this.updateById(task);
        return ResultVO.judge(success);
    }

    @Override
    public List<Map<String, Object>> getBackNodes(Long taskId) {
        return List.of();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> backTask(Long taskId, String targetNodeId, String reason) {
        return ResultVO.success(true);
    }

    @Override
    public List<Map<String, Object>> getTaskHistory(Long instanceId) {
        return workflowHistoryService.getHistoryByInstanceId(instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> markTaskAsRead(Long taskId) {
        boolean success = workflowTaskMapper.markAsRead(taskId) > 0;
        return ResultVO.judge(success);
    }

    @Override
    public Long getTodoCount(Long userId) {
        if (userId == null) {
            userId = SecurityUtils.getUserId();
        }
        return workflowTaskMapper.countTodoByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> claimTask(Long taskId) {
        WorkflowTask workflowTask = this.getById(taskId);
        if (workflowTask == null) {
            return ResultVO.error("任务不存在");
        }

        try {
            taskService.claim(workflowTask.getTaskId(), String.valueOf(SecurityUtils.getUserId()));

            workflowTask.setAssigneeId(SecurityUtils.getUserId());
            SysUser user = sysUserService.getById(SecurityUtils.getUserId());
            workflowTask.setAssigneeName(user != null ? user.getRealName() : "");
            this.updateById(workflowTask);

            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("签收任务失败", e);
            return ResultVO.error("签收任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> unclaimTask(Long taskId) {
        WorkflowTask workflowTask = this.getById(taskId);
        if (workflowTask == null) {
            return ResultVO.error("任务不存在");
        }

        try {
            taskService.unclaim(workflowTask.getTaskId());

            workflowTask.setAssigneeId(null);
            workflowTask.setAssigneeName(null);
            this.updateById(workflowTask);

            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("取消签收失败", e);
            return ResultVO.error("取消签收失败: " + e.getMessage());
        }
    }

    private List<String> getUserCandidateGroups(Long userId) {
        return List.of();
    }

    private void updateInstanceCurrentNode(Long instanceId) {
        WorkflowInstance instance = workflowInstanceMapper.selectById(instanceId);
        if (instance == null) {
            return;
        }

        Task nextTask = taskService.createTaskQuery()
                .processInstanceId(instance.getProcessInstanceId())
                .singleResult();

        if (nextTask != null) {
            instance.setCurrentNodeId(nextTask.getTaskDefinitionKey());
            instance.setCurrentNodeName(nextTask.getName());
            instance.setCurrentAssigneeId(nextTask.getAssignee() != null ? Long.valueOf(nextTask.getAssignee()) : null);

            if (nextTask.getAssignee() != null) {
                SysUser user = sysUserService.getById(Long.valueOf(nextTask.getAssignee()));
                instance.setCurrentAssigneeName(user != null ? user.getRealName() : "");
            }

            workflowInstanceMapper.updateById(instance);
        } else {
            instance.setStatus(1);
            instance.setResult(1);
            instance.setEndTime(LocalDateTime.now());
            instance.setDuration(Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis());
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName(null);
            instance.setCurrentAssigneeId(null);
            instance.setCurrentAssigneeName(null);
            workflowInstanceMapper.updateById(instance);

            workflowCallbackService.onProcessEnd(instanceId, Map.of("result", 1));
        }
    }

    private WorkflowTaskVO convertToVO(WorkflowTask task) {
        WorkflowTaskVO vo = new WorkflowTaskVO();
        BeanUtils.copyProperties(task, vo);

        if (task.getStatus() != null) {
            vo.setStatusName(getStatusName(task.getStatus()));
        }
        if (task.getResult() != null) {
            vo.setResultName(getResultName(task.getResult()));
        }
        if (task.getTaskType() != null) {
            vo.setTaskTypeName(getTaskTypeName(task.getTaskType()));
        }

        if (task.getDuration() != null && task.getDuration() > 0) {
            vo.setDurationText(formatDuration(task.getDuration()));
        }

        if (task.getAssigneeId() != null) {
            SysUser user = sysUserService.getById(task.getAssigneeId());
            if (user != null) {
                vo.setAssigneeAvatar(user.getAvatar());
            }
        }

        WorkflowInstance instance = workflowInstanceMapper.selectById(task.getInstanceId());
        if (instance != null) {
            vo.setProcessName(instance.getBusinessTitle());
            vo.setBusinessTitle(instance.getBusinessTitle());
            vo.setBusinessType(instance.getBusinessType());
            vo.setApplicantId(instance.getApplicantId());
            vo.setApplicantName(instance.getApplicantName());

            SysUser applicant = sysUserService.getById(instance.getApplicantId());
            if (applicant != null) {
                vo.setApplicantAvatar(applicant.getAvatar());
            }
        }

        if (task.getFormData() != null) {
            try {
                vo.setFormData(objectMapper.readValue(task.getFormData(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败", e);
            }
        }

        return vo;
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "已处理";
            case 2 -> "已转办";
            case 3 -> "已委派";
            default -> "未知";
        };
    }

    private String getResultName(Integer result) {
        return switch (result) {
            case 0 -> "待审批";
            case 1 -> "通过";
            case 2 -> "驳回";
            case 3 -> "转办";
            case 4 -> "委派";
            case 5 -> "撤回";
            default -> "未知";
        };
    }

    private String getTaskTypeName(Integer taskType) {
        return switch (taskType) {
            case 0 -> "审批任务";
            case 1 -> "抄送任务";
            case 2 -> "办理任务";
            default -> "未知";
        };
    }

    private String formatDuration(Long duration) {
        if (duration == null || duration == 0) {
            return "-";
        }

        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "天" + (hours % 24) + "小时";
        } else if (hours > 0) {
            return hours + "小时" + (minutes % 60) + "分钟";
        } else if (minutes > 0) {
            return minutes + "分钟" + (seconds % 60) + "秒";
        } else {
            return seconds + "秒";
        }
    }
}
