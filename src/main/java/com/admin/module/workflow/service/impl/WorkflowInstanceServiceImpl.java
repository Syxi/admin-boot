package com.admin.module.workflow.service.impl;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.entity.SysUser;
import com.admin.module.system.service.SysUserService;
import com.admin.module.workflow.entity.WorkflowDefinition;
import com.admin.module.workflow.entity.WorkflowHistory;
import com.admin.module.workflow.entity.WorkflowInstance;
import com.admin.module.workflow.entity.WorkflowTask;
import com.admin.module.workflow.form.StartProcessForm;
import com.admin.module.workflow.mapper.WorkflowDefinitionMapper;
import com.admin.module.workflow.mapper.WorkflowInstanceMapper;
import com.admin.module.workflow.mapper.WorkflowTaskMapper;
import com.admin.module.workflow.query.WorkflowInstanceQuery;
import com.admin.module.workflow.service.*;
import com.admin.module.workflow.vo.WorkflowInstanceVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程实例服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowInstanceServiceImpl extends ServiceImpl<WorkflowInstanceMapper, WorkflowInstance>
        implements WorkflowInstanceService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final WorkflowDefinitionMapper workflowDefinitionMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final WorkflowTaskMapper workflowTaskMapper;
    private final WorkflowHistoryService workflowHistoryService;
    private final WorkflowCallbackService workflowCallbackService;
    private final SysUserService sysUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResult<WorkflowInstanceVO> selectInstancePage(WorkflowInstanceQuery query) {
        Page<WorkflowInstance> page = new Page<>(query.getPage(), query.getLimit());
        IPage<WorkflowInstance> result = workflowInstanceMapper.selectInstancePage(page,
                query.getDefinitionId(), query.getApplicantId(), query.getStatus(),
                query.getBusinessType(), query.getBusinessTitle(), SecurityUtils.getTenantId());

        IPage<WorkflowInstanceVO> voPage = result.convert(this::convertToVO);

        return PageResult.success(voPage);
    }

    @Override
    public WorkflowInstanceVO getInstanceById(Long id) {
        WorkflowInstance instance = this.getById(id);
        if (instance == null) {
            return null;
        }
        return convertToVO(instance);
    }

    @Override
    public WorkflowInstanceVO getInstanceByBusinessKey(String businessKey) {
        WorkflowInstance instance = workflowInstanceMapper.selectByBusinessKey(businessKey);
        if (instance == null) {
            return null;
        }
        return convertToVO(instance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<WorkflowInstanceVO> startProcessInstance(StartProcessForm form) {
        WorkflowDefinition definition = workflowDefinitionMapper.selectById(form.getDefinitionId());
        if (definition == null) {
            return ResultVO.error("流程定义不存在");
        }

        if (definition.getStatus() != 1) {
            return ResultVO.error("流程未发布");
        }

        try {
            Long currentUserId = SecurityUtils.getUserId();
            SysUser user = sysUserService.getById(currentUserId);

            Map<String, Object> variables = new HashMap<>();
            variables.put("applicantId", currentUserId);
            variables.put("applicantName", user != null ? user.getRealName() : "");
            variables.put("deptId", user != null ? user.getDeptId() : null);
            variables.put("tenantId", SecurityUtils.getTenantId());

            if (form.getVariables() != null) {
                variables.putAll(form.getVariables());
            }

            ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionId(definition.getFlowableDefinitionId())
                    .businessKey(form.getBusinessKey())
                    .variables(variables)
                    .tenantId(String.valueOf(SecurityUtils.getTenantId()))
                    .start();

            WorkflowInstance instance = new WorkflowInstance();
            instance.setDefinitionId(form.getDefinitionId());
            instance.setProcessInstanceId(processInstance.getId());
            instance.setBusinessType(form.getBusinessType());
            instance.setBusinessKey(form.getBusinessKey());
            instance.setBusinessTitle(form.getBusinessTitle());
            instance.setApplicantId(currentUserId);
            instance.setApplicantName(user != null ? user.getRealName() : "");
            instance.setDeptId(user != null ? user.getDeptId() : null);
            instance.setStatus(0);
            instance.setResult(0);
            instance.setStartTime(LocalDateTime.now());
            instance.setTenantId(SecurityUtils.getTenantId());
            instance.setRemark(form.getRemark());

            if (form.getFormData() != null) {
                try {
                    instance.setFormData(objectMapper.writeValueAsString(form.getFormData()));
                } catch (JsonProcessingException e) {
                    log.error("JSON序列化失败", e);
                }
            }

            this.save(instance);

            workflowHistoryService.addHistory(instance.getId(), null, "start", "开始节点",
                    1, currentUserId, user != null ? user.getRealName() : "",
                    "发起流程", 1, variables);

            workflowCallbackService.onProcessStart(instance.getId(), variables);

            Task task = taskService.createTaskQuery()
                    .processInstanceId(processInstance.getId())
                    .singleResult();

            if (task != null) {
                instance.setCurrentNodeId(task.getTaskDefinitionKey());
                instance.setCurrentNodeName(task.getName());
                this.updateById(instance);
            }

            return ResultVO.success(convertToVO(instance));
        } catch (Exception e) {
            log.error("启动流程失败", e);
            return ResultVO.error("启动流程失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> terminateInstance(Long id, String reason) {
        WorkflowInstance instance = this.getById(id);
        if (instance == null) {
            return ResultVO.error("流程实例不存在");
        }

        try {
            runtimeService.deleteProcessInstance(instance.getProcessInstanceId(), reason);

            instance.setStatus(2);
            instance.setResult(3);
            instance.setEndTime(LocalDateTime.now());
            instance.setDuration(calculateDuration(instance.getStartTime(), instance.getEndTime()));
            this.updateById(instance);

            Long currentUserId = SecurityUtils.getUserId();
            SysUser user = sysUserService.getById(currentUserId);

            workflowHistoryService.addHistory(instance.getId(), null, "end", "结束节点",
                    7, currentUserId, user != null ? user.getRealName() : "",
                    reason, 3, null);

            workflowCallbackService.onProcessEnd(instance.getId(), Map.of("reason", reason, "result", 3));

            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("终止流程失败", e);
            return ResultVO.error("终止流程失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> deleteInstance(Long id) {
        WorkflowInstance instance = this.getById(id);
        if (instance == null) {
            return ResultVO.error("流程实例不存在");
        }

        try {
            if (instance.getStatus() == 0) {
                runtimeService.deleteProcessInstance(instance.getProcessInstanceId(), "删除流程实例");
            }

            this.removeById(id);
            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("删除流程实例失败", e);
            return ResultVO.error("删除流程实例失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> revokeInstance(Long id, String reason) {
        WorkflowInstance instance = this.getById(id);
        if (instance == null) {
            return ResultVO.error("流程实例不存在");
        }

        if (!instance.getApplicantId().equals(SecurityUtils.getUserId())) {
            return ResultVO.error("只有发起人才能撤回流程");
        }

        try {
            runtimeService.deleteProcessInstance(instance.getProcessInstanceId(), reason);

            instance.setStatus(2);
            instance.setResult(4);
            instance.setEndTime(LocalDateTime.now());
            instance.setDuration(calculateDuration(instance.getStartTime(), instance.getEndTime()));
            this.updateById(instance);

            Long currentUserId = SecurityUtils.getUserId();
            SysUser user = sysUserService.getById(currentUserId);

            workflowHistoryService.addHistory(instance.getId(), null, "end", "结束节点",
                    6, currentUserId, user != null ? user.getRealName() : "",
                    reason, 4, null);

            workflowCallbackService.onProcessEnd(instance.getId(), Map.of("reason", reason, "result", 4));

            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("撤回流程失败", e);
            return ResultVO.error("撤回流程失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> suspendInstance(Long id) {
        WorkflowInstance instance = this.getById(id);
        if (instance == null) {
            return ResultVO.error("流程实例不存在");
        }

        try {
            runtimeService.suspendProcessInstanceById(instance.getProcessInstanceId());
            instance.setStatus(3);
            this.updateById(instance);
            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("挂起流程失败", e);
            return ResultVO.error("挂起流程失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> activateInstance(Long id) {
        WorkflowInstance instance = this.getById(id);
        if (instance == null) {
            return ResultVO.error("流程实例不存在");
        }

        try {
            runtimeService.activateProcessInstanceById(instance.getProcessInstanceId());
            instance.setStatus(0);
            this.updateById(instance);
            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("激活流程失败", e);
            return ResultVO.error("激活流程失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> transferInstance(Long id, Long targetUserId, String reason) {
        return ResultVO.success(true);
    }

    @Override
    public Map<String, Object> getProcessProgress(Long id) {
        WorkflowInstance instance = this.getById(id);
        if (instance == null) {
            return Map.of();
        }

        Map<String, Object> progress = new HashMap<>();
        progress.put("instanceId", id);
        progress.put("processInstanceId", instance.getProcessInstanceId());
        progress.put("status", instance.getStatus());
        progress.put("result", instance.getResult());
        progress.put("currentNodeId", instance.getCurrentNodeId());
        progress.put("currentNodeName", instance.getCurrentNodeName());

        List<WorkflowHistory> historyList = workflowHistoryService.lambdaQuery()
                .eq(WorkflowHistory::getInstanceId, id)
                .eq(WorkflowHistory::getDeleted, 0)
                .orderByAsc(WorkflowHistory::getOperationTime)
                .list();

        progress.put("historyList", historyList);

        return progress;
    }

    @Override
    public PageResult<WorkflowInstanceVO> getMyStartedInstances(WorkflowInstanceQuery query) {
        query.setApplicantId(SecurityUtils.getUserId());
        return selectInstancePage(query);
    }

    @Override
    public Map<String, Object> getVariables(Long id) {
        WorkflowInstance instance = this.getById(id);
        if (instance == null) {
            return Map.of();
        }

        Map<String, Object> variables = runtimeService.getVariables(instance.getProcessInstanceId());
        return variables != null ? variables : Map.of();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> updateVariables(Long id, Map<String, Object> variables) {
        WorkflowInstance instance = this.getById(id);
        if (instance == null) {
            return ResultVO.error("流程实例不存在");
        }

        try {
            runtimeService.setVariables(instance.getProcessInstanceId(), variables);
            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("更新流程变量失败", e);
            return ResultVO.error("更新流程变量失败: " + e.getMessage());
        }
    }

    @Override
    public WorkflowInstance getByProcessInstanceId(String processInstanceId) {
        return workflowInstanceMapper.selectByProcessInstanceId(processInstanceId);
    }

    private WorkflowInstanceVO convertToVO(WorkflowInstance instance) {
        WorkflowInstanceVO vo = new WorkflowInstanceVO();
        BeanUtils.copyProperties(instance, vo);

        if (instance.getDefinitionId() != null) {
            WorkflowDefinition definition = workflowDefinitionMapper.selectById(instance.getDefinitionId());
            if (definition != null) {
                vo.setProcessName(definition.getProcessName());
                vo.setProcessKey(definition.getProcessKey());
            }
        }

        if (instance.getStatus() != null) {
            vo.setStatusName(getStatusName(instance.getStatus()));
        }
        if (instance.getResult() != null) {
            vo.setResultName(getResultName(instance.getResult()));
        }

        if (instance.getDuration() != null) {
            vo.setDurationText(formatDuration(instance.getDuration()));
        }

        if (instance.getApplicantId() != null) {
            SysUser user = sysUserService.getById(instance.getApplicantId());
            if (user != null) {
                vo.setApplicantAvatar(user.getAvatar());
            }
        }

        if (instance.getCurrentAssigneeId() != null) {
            SysUser user = sysUserService.getById(instance.getCurrentAssigneeId());
            if (user != null) {
                vo.setCurrentAssigneeAvatar(user.getAvatar());
            }
        }

        if (instance.getFormData() != null) {
            try {
                vo.setFormData(objectMapper.readValue(instance.getFormData(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败", e);
            }
        }
        if (instance.getVariables() != null) {
            try {
                vo.setVariables(objectMapper.readValue(instance.getVariables(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败", e);
            }
        }

        return vo;
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "运行中";
            case 1 -> "已完成";
            case 2 -> "已终止";
            case 3 -> "已挂起";
            default -> "未知";
        };
    }

    private String getResultName(Integer result) {
        return switch (result) {
            case 0 -> "审批中";
            case 1 -> "已通过";
            case 2 -> "已驳回";
            case 3 -> "已终止";
            case 4 -> "已撤回";
            default -> "未知";
        };
    }

    private Long calculateDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0L;
        }
        return Duration.between(startTime, endTime).toMillis();
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
