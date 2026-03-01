package com.admin.module.workflow.service.impl;

import com.admin.module.workflow.entity.WorkflowHistory;
import com.admin.module.workflow.mapper.WorkflowHistoryMapper;
import com.admin.module.workflow.service.WorkflowHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程历史记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowHistoryServiceImpl extends ServiceImpl<WorkflowHistoryMapper, WorkflowHistory>
        implements WorkflowHistoryService {

    private final WorkflowHistoryMapper workflowHistoryMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Map<String, Object>> getHistoryByInstanceId(Long instanceId) {
        List<WorkflowHistory> historyList = workflowHistoryMapper.selectByInstanceId(instanceId);
        return historyList.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getHistoryByTaskId(Long taskId) {
        List<WorkflowHistory> historyList = workflowHistoryMapper.selectByTaskId(taskId);
        return historyList.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    @Override
    public void addHistory(Long instanceId, Long taskId, String nodeId, String nodeName,
                          Integer operationType, Long operatorId, String operatorName,
                          String comment, Integer result, Map<String, Object> variables) {
        WorkflowHistory history = new WorkflowHistory();
        history.setInstanceId(instanceId);
        history.setTaskId(taskId);
        history.setNodeId(nodeId);
        history.setNodeName(nodeName);
        history.setOperationType(operationType);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setComment(comment);
        history.setResult(result);
        history.setOperationTime(LocalDateTime.now());

        if (variables != null) {
            try {
                history.setVariables(objectMapper.writeValueAsString(variables));
            } catch (JsonProcessingException e) {
                log.error("JSON序列化失败", e);
            }
        }

        this.save(history);
    }

    @Override
    public List<Map<String, Object>> getApprovalTrack(Long instanceId) {
        List<WorkflowHistory> historyList = workflowHistoryMapper.selectByInstanceId(instanceId);

        return historyList.stream()
                .filter(h -> h.getOperationType() != null && h.getOperationType() != 9)
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getTimeline(Long instanceId) {
        List<WorkflowHistory> historyList = workflowHistoryMapper.selectByInstanceId(instanceId);

        return historyList.stream()
                .map(h -> {
                    Map<String, Object> map = convertToMap(h);

                    if (h.getOperationType() != null) {
                        map.put("operationTypeName", getOperationTypeName(h.getOperationType()));
                    }
                    if (h.getResult() != null) {
                        map.put("resultName", getResultName(h.getResult()));
                    }

                    map.put("icon", getOperationIcon(h.getOperationType()));
                    map.put("color", getOperationColor(h.getOperationType(), h.getResult()));

                    return map;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertToMap(WorkflowHistory history) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", history.getId());
        map.put("instanceId", history.getInstanceId());
        map.put("taskId", history.getTaskId());
        map.put("nodeId", history.getNodeId());
        map.put("nodeName", history.getNodeName());
        map.put("operationType", history.getOperationType());
        map.put("operatorId", history.getOperatorId());
        map.put("operatorName", history.getOperatorName());
        map.put("operatorDept", history.getOperatorDept());
        map.put("comment", history.getComment());
        map.put("result", history.getResult());
        map.put("operationTime", history.getOperationTime());
        map.put("duration", history.getDuration());
        map.put("targetNodeId", history.getTargetNodeId());
        map.put("targetNodeName", history.getTargetNodeName());
        map.put("targetAssigneeId", history.getTargetAssigneeId());
        map.put("targetAssigneeName", history.getTargetAssigneeName());

        if (history.getFormData() != null) {
            try {
                map.put("formData", objectMapper.readValue(history.getFormData(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败", e);
            }
        }
        if (history.getVariables() != null) {
            try {
                map.put("variables", objectMapper.readValue(history.getVariables(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败", e);
            }
        }
        if (history.getAttachments() != null) {
            try {
                map.put("attachments", objectMapper.readValue(history.getAttachments(), new TypeReference<List<Map<String, Object>>>() {}));
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败", e);
            }
        }

        return map;
    }

    private String getOperationTypeName(Integer operationType) {
        return switch (operationType) {
            case 1 -> "提交";
            case 2 -> "审批通过";
            case 3 -> "审批驳回";
            case 4 -> "转办";
            case 5 -> "委派";
            case 6 -> "撤回";
            case 7 -> "终止";
            case 8 -> "抄送";
            case 9 -> "评论";
            default -> "未知";
        };
    }

    private String getResultName(Integer result) {
        return switch (result) {
            case 1 -> "通过";
            case 2 -> "驳回";
            default -> "";
        };
    }

    private String getOperationIcon(Integer operationType) {
        return switch (operationType) {
            case 1 -> "el-icon-video-play";
            case 2 -> "el-icon-check";
            case 3 -> "el-icon-close";
            case 4 -> "el-icon-right";
            case 5 -> "el-icon-user";
            case 6 -> "el-icon-refresh-left";
            case 7 -> "el-icon-circle-close";
            case 8 -> "el-icon-message";
            case 9 -> "el-icon-chat-dot-round";
            default -> "el-icon-more";
        };
    }

    private String getOperationColor(Integer operationType, Integer result) {
        return switch (operationType) {
            case 1 -> "#409EFF";
            case 2 -> "#67C23A";
            case 3 -> "#F56C6C";
            case 4 -> "#E6A23C";
            case 5 -> "#909399";
            case 6 -> "#409EFF";
            case 7 -> "#F56C6C";
            case 8 -> "#909399";
            case 9 -> "#409EFF";
            default -> "#909399";
        };
    }
}
