package com.admin.module.workflow.service.impl;

import com.admin.module.workflow.entity.WorkflowCallback;
import com.admin.module.workflow.mapper.WorkflowCallbackMapper;
import com.admin.module.workflow.service.WorkflowCallbackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程回调服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowCallbackServiceImpl extends ServiceImpl<WorkflowCallbackMapper, WorkflowCallback>
        implements WorkflowCallbackService {

    private final WorkflowCallbackMapper workflowCallbackMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void createCallback(Long instanceId, String businessType, String businessKey,
                              Integer callbackType, String callbackUrl, Map<String, Object> callbackData) {
        WorkflowCallback callback = new WorkflowCallback();
        callback.setInstanceId(instanceId);
        callback.setBusinessType(businessType);
        callback.setBusinessKey(businessKey);
        callback.setCallbackType(callbackType);
        callback.setCallbackUrl(callbackUrl);
        try {
            callback.setCallbackData(objectMapper.writeValueAsString(callbackData));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            callback.setCallbackData("{}");
        }
        callback.setStatus(0);
        callback.setRetryCount(0);
        callback.setMaxRetry(3);

        this.save(callback);

        sendCallback(callback.getId());
    }

    @Override
    @Async
    public void sendCallback(Long callbackId) {
        WorkflowCallback callback = this.getById(callbackId);
        if (callback == null || callback.getStatus() == 1) {
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> data = new HashMap<>();
            data.put("instanceId", callback.getInstanceId());
            data.put("businessType", callback.getBusinessType());
            data.put("businessKey", callback.getBusinessKey());
            data.put("callbackType", callback.getCallbackType());
            data.put("callbackTypeName", getCallbackTypeName(callback.getCallbackType()));
            try {
                data.put("data", objectMapper.readValue(callback.getCallbackData(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败", e);
                data.put("data", new HashMap<>());
            }
            data.put("timestamp", System.currentTimeMillis());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    callback.getCallbackUrl(), request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                callback.setStatus(1);
                callback.setResponseResult(response.getBody());
                log.info("回调发送成功: {}", callback.getCallbackUrl());
            } else {
                callback.setStatus(2);
                callback.setResponseResult("HTTP " + response.getStatusCode());
                log.warn("回调发送失败: {}, 状态码: {}", callback.getCallbackUrl(), response.getStatusCode());
            }
        } catch (Exception e) {
            callback.setStatus(2);
            callback.setResponseResult(e.getMessage());
            log.error("回调发送异常: {}", callback.getCallbackUrl(), e);
        }

        callback.setRetryCount(callback.getRetryCount() + 1);
        callback.setSendTime(LocalDateTime.now());
        this.updateById(callback);
    }

    @Override
    public void onProcessStart(Long instanceId, Map<String, Object> data) {
        log.info("流程开始回调: instanceId={}, data={}", instanceId, data);

        WorkflowCallback callback = new WorkflowCallback();
        callback.setInstanceId(instanceId);
        callback.setCallbackType(1);
        try {
            callback.setCallbackData(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            callback.setCallbackData("{}");
        }
        callback.setStatus(0);
        callback.setRetryCount(0);
        callback.setMaxRetry(3);

        this.save(callback);
    }

    @Override
    public void onProcessEnd(Long instanceId, Map<String, Object> data) {
        log.info("流程结束回调: instanceId={}, data={}", instanceId, data);

        WorkflowCallback callback = new WorkflowCallback();
        callback.setInstanceId(instanceId);
        callback.setCallbackType(2);
        try {
            callback.setCallbackData(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            callback.setCallbackData("{}");
        }
        callback.setStatus(0);
        callback.setRetryCount(0);
        callback.setMaxRetry(3);

        this.save(callback);
    }

    @Override
    public void onNodeEnter(Long instanceId, String nodeId, Map<String, Object> data) {
        log.info("节点进入回调: instanceId={}, nodeId={}, data={}", instanceId, nodeId, data);

        Map<String, Object> callbackData = new HashMap<>(data);
        callbackData.put("nodeId", nodeId);

        WorkflowCallback callback = new WorkflowCallback();
        callback.setInstanceId(instanceId);
        callback.setCallbackType(3);
        try {
            callback.setCallbackData(objectMapper.writeValueAsString(callbackData));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            callback.setCallbackData("{}");
        }
        callback.setStatus(0);
        callback.setRetryCount(0);
        callback.setMaxRetry(3);

        this.save(callback);
    }

    @Override
    public void onNodeComplete(Long instanceId, String nodeId, Map<String, Object> data) {
        log.info("节点完成回调: instanceId={}, nodeId={}, data={}", instanceId, nodeId, data);

        Map<String, Object> callbackData = new HashMap<>(data);
        callbackData.put("nodeId", nodeId);

        WorkflowCallback callback = new WorkflowCallback();
        callback.setInstanceId(instanceId);
        callback.setCallbackType(4);
        try {
            callback.setCallbackData(objectMapper.writeValueAsString(callbackData));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            callback.setCallbackData("{}");
        }
        callback.setStatus(0);
        callback.setRetryCount(0);
        callback.setMaxRetry(3);

        this.save(callback);
    }

    @Override
    public void onTaskAssign(Long instanceId, Long taskId, Map<String, Object> data) {
        log.info("任务分配回调: instanceId={}, taskId={}, data={}", instanceId, taskId, data);

        Map<String, Object> callbackData = new HashMap<>(data);
        callbackData.put("taskId", taskId);

        WorkflowCallback callback = new WorkflowCallback();
        callback.setInstanceId(instanceId);
        callback.setCallbackType(5);
        try {
            callback.setCallbackData(objectMapper.writeValueAsString(callbackData));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            callback.setCallbackData("{}");
        }
        callback.setStatus(0);
        callback.setRetryCount(0);
        callback.setMaxRetry(3);

        this.save(callback);
    }

    @Override
    public void onApprovalResult(Long instanceId, Long taskId, Integer result, Map<String, Object> data) {
        log.info("审批结果回调: instanceId={}, taskId={}, result={}, data={}", instanceId, taskId, result, data);

        Map<String, Object> callbackData = new HashMap<>(data);
        callbackData.put("taskId", taskId);
        callbackData.put("result", result);
        callbackData.put("resultName", result == 1 ? "通过" : "驳回");

        WorkflowCallback callback = new WorkflowCallback();
        callback.setInstanceId(instanceId);
        callback.setCallbackType(6);
        try {
            callback.setCallbackData(objectMapper.writeValueAsString(callbackData));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            callback.setCallbackData("{}");
        }
        callback.setStatus(0);
        callback.setRetryCount(0);
        callback.setMaxRetry(3);

        this.save(callback);
    }

    @Override
    public void retryFailedCallbacks() {
        // 查询失败的回调记录（status = 2）
        List<WorkflowCallback> allCallbacks = workflowCallbackMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkflowCallback>()
                        .eq(WorkflowCallback::getStatus, 2)
                        .eq(WorkflowCallback::getDeleted, 0)
        );
        for (WorkflowCallback callback : allCallbacks) {
            if (callback.getRetryCount() < callback.getMaxRetry()) {
                sendCallback(callback.getId());
            }
        }
    }

    /**
     * 获取回调类型名称
     */
    private String getCallbackTypeName(Integer callbackType) {
        switch (callbackType) {
            case 1: return "流程开始";
            case 2: return "流程结束";
            case 3: return "节点进入";
            case 4: return "节点完成";
            case 5: return "任务分配";
            case 6: return "审批结果";
            default: return "未知";
        }
    }
}
