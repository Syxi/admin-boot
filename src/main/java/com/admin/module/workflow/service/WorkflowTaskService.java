package com.admin.module.workflow.service;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.workflow.entity.WorkflowTask;
import com.admin.module.workflow.vo.WorkflowTaskVO;
import com.admin.module.workflow.form.TaskCompleteForm;
import com.admin.module.workflow.form.TaskTransferForm;
import com.admin.module.workflow.form.TaskDelegateForm;
import com.admin.module.workflow.query.WorkflowTaskQuery;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 流程任务服务接口
 */
public interface WorkflowTaskService extends IService<WorkflowTask> {

    /**
     * 分页查询待办任务
     */
    PageResult<WorkflowTaskVO> selectTodoPage(WorkflowTaskQuery query);

    /**
     * 分页查询已办任务
     */
    PageResult<WorkflowTaskVO> selectDonePage(WorkflowTaskQuery query);

    /**
     * 获取任务详情
     */
    WorkflowTaskVO getTaskById(Long id);

    /**
     * 根据Flowable任务ID获取
     */
    WorkflowTask getByFlowableTaskId(String taskId);

    /**
     * 完成任务
     */
    ResultVO<Boolean> completeTask(TaskCompleteForm form);

    /**
     * 驳回任务
     */
    ResultVO<Boolean> rejectTask(TaskCompleteForm form);

    /**
     * 转办任务
     */
    ResultVO<Boolean> transferTask(TaskTransferForm form);

    /**
     * 委派任务
     */
    ResultVO<Boolean> delegateTask(TaskDelegateForm form);

    /**
     * 撤回任务
     */
    ResultVO<Boolean> revokeTask(Long taskId, String reason);

    /**
     * 获取任务表单数据
     */
    Map<String, Object> getTaskFormData(Long taskId);

    /**
     * 保存任务表单数据
     */
    ResultVO<Boolean> saveTaskFormData(Long taskId, Map<String, Object> formData);

    /**
     * 获取可退回的节点
     */
    List<Map<String, Object>> getBackNodes(Long taskId);

    /**
     * 退回任务到指定节点
     */
    ResultVO<Boolean> backTask(Long taskId, String targetNodeId, String reason);

    /**
     * 获取任务审批历史
     */
    List<Map<String, Object>> getTaskHistory(Long instanceId);

    /**
     * 标记任务已读
     */
    ResultVO<Boolean> markTaskAsRead(Long taskId);

    /**
     * 获取待办任务数量
     */
    Long getTodoCount(Long userId);

    /**
     * 签收任务
     */
    ResultVO<Boolean> claimTask(Long taskId);

    /**
     * 取消签收
     */
    ResultVO<Boolean> unclaimTask(Long taskId);
}
