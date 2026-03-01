package com.admin.module.workflow.service;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.workflow.entity.WorkflowCarbonCopy;
import com.admin.module.workflow.vo.WorkflowCarbonCopyVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 流程抄送服务接口
 */
public interface WorkflowCarbonCopyService extends IService<WorkflowCarbonCopy> {

    /**
     * 分页查询抄送记录
     */
    PageResult<WorkflowCarbonCopyVO> selectCcPage(Long ccUserId, Integer isRead, Integer pageNum, Integer pageSize);

    /**
     * 添加抄送记录
     */
    void addCarbonCopy(Long instanceId, Long taskId, String nodeId, String nodeName, 
                      List<Long> ccUserIds, String remark);

    /**
     * 标记已读
     */
    ResultVO<Boolean> markAsRead(Long id);

    /**
     * 批量标记已读
     */
    ResultVO<Boolean> markAllAsRead(Long ccUserId);

    /**
     * 获取未读抄送数量
     */
    Long getUnreadCount(Long ccUserId);

    /**
     * 获取流程的抄送记录
     */
    List<WorkflowCarbonCopyVO> getCcByInstanceId(Long instanceId);
}
