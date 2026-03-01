package com.admin.module.workflow.service;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.workflow.entity.WorkflowInstance;
import com.admin.module.workflow.vo.WorkflowInstanceVO;
import com.admin.module.workflow.form.StartProcessForm;
import com.admin.module.workflow.query.WorkflowInstanceQuery;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 流程实例服务接口
 */
public interface WorkflowInstanceService extends IService<WorkflowInstance> {

    /**
     * 分页查询流程实例
     */
    PageResult<WorkflowInstanceVO> selectInstancePage(WorkflowInstanceQuery query);

    /**
     * 获取流程实例详情
     */
    WorkflowInstanceVO getInstanceById(Long id);

    /**
     * 根据业务Key获取流程实例
     */
    WorkflowInstanceVO getInstanceByBusinessKey(String businessKey);

    /**
     * 启动流程实例
     */
    ResultVO<WorkflowInstanceVO> startProcessInstance(StartProcessForm form);

    /**
     * 终止流程实例
     */
    ResultVO<Boolean> terminateInstance(Long id, String reason);

    /**
     * 删除流程实例
     */
    ResultVO<Boolean> deleteInstance(Long id);

    /**
     * 撤回流程实例
     */
    ResultVO<Boolean> revokeInstance(Long id, String reason);

    /**
     * 挂起流程实例
     */
    ResultVO<Boolean> suspendInstance(Long id);

    /**
     * 激活流程实例
     */
    ResultVO<Boolean> activateInstance(Long id);

    /**
     * 转交流程实例
     */
    ResultVO<Boolean> transferInstance(Long id, Long targetUserId, String reason);

    /**
     * 获取流程进度
     */
    Map<String, Object> getProcessProgress(Long id);

    /**
     * 获取我的发起的流程
     */
    PageResult<WorkflowInstanceVO> getMyStartedInstances(WorkflowInstanceQuery query);

    /**
     * 获取流程变量
     */
    Map<String, Object> getVariables(Long id);

    /**
     * 更新流程变量
     */
    ResultVO<Boolean> updateVariables(Long id, Map<String, Object> variables);

    /**
     * 根据流程实例ID获取
     */
    WorkflowInstance getByProcessInstanceId(String processInstanceId);
}
