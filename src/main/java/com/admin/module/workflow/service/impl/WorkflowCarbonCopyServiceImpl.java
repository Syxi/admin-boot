package com.admin.module.workflow.service.impl;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysUser;
import com.admin.module.system.service.SysUserService;
import com.admin.module.workflow.entity.WorkflowCarbonCopy;
import com.admin.module.workflow.entity.WorkflowInstance;
import com.admin.module.workflow.mapper.WorkflowCarbonCopyMapper;
import com.admin.module.workflow.mapper.WorkflowInstanceMapper;
import com.admin.module.workflow.service.WorkflowCarbonCopyService;
import com.admin.module.workflow.vo.WorkflowCarbonCopyVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程抄送服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowCarbonCopyServiceImpl extends ServiceImpl<WorkflowCarbonCopyMapper, WorkflowCarbonCopy>
        implements WorkflowCarbonCopyService {

    private final WorkflowCarbonCopyMapper workflowCarbonCopyMapper;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final SysUserService sysUserService;

    @Override
    public PageResult<WorkflowCarbonCopyVO> selectCcPage(Long ccUserId, Integer isRead, Integer pageNum, Integer pageSize) {
        Page<WorkflowCarbonCopy> page = new Page<>(pageNum, pageSize);

        IPage<WorkflowCarbonCopy> result = workflowCarbonCopyMapper.selectCcPage(page, ccUserId, isRead);

        IPage<WorkflowCarbonCopyVO> voPage = result.convert(this::convertToVO);

        return PageResult.success(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCarbonCopy(Long instanceId, Long taskId, String nodeId, String nodeName,
                             List<Long> ccUserIds, String remark) {
        if (ccUserIds == null || ccUserIds.isEmpty()) {
            return;
        }

        for (Long ccUserId : ccUserIds) {
            SysUser user = sysUserService.getById(ccUserId);

            WorkflowCarbonCopy cc = new WorkflowCarbonCopy();
            cc.setInstanceId(instanceId);
            cc.setTaskId(taskId);
            cc.setNodeId(nodeId);
            cc.setNodeName(nodeName);
            cc.setCcUserId(ccUserId);
            cc.setCcUserName(user != null ? user.getRealName() : "");
            cc.setIsRead(0);
            cc.setCcTime(LocalDateTime.now());
            cc.setRemark(remark);

            this.save(cc);
        }

        log.info("添加抄送记录: instanceId={}, ccUserIds={}", instanceId, ccUserIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> markAsRead(Long id) {
        boolean success = workflowCarbonCopyMapper.markAsRead(id) > 0;
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> markAllAsRead(Long ccUserId) {
        boolean success = workflowCarbonCopyMapper.markAllAsRead(ccUserId) > 0;
        return ResultVO.judge(success);
    }

    @Override
    public Long getUnreadCount(Long ccUserId) {
        return workflowCarbonCopyMapper.countUnreadByUserId(ccUserId);
    }

    @Override
    public List<WorkflowCarbonCopyVO> getCcByInstanceId(Long instanceId) {
        List<WorkflowCarbonCopy> ccList = workflowCarbonCopyMapper.selectByInstanceId(instanceId);
        return ccList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private WorkflowCarbonCopyVO convertToVO(WorkflowCarbonCopy cc) {
        WorkflowCarbonCopyVO vo = new WorkflowCarbonCopyVO();
        BeanUtils.copyProperties(cc, vo);

        if (cc.getCcUserId() != null) {
            SysUser user = sysUserService.getById(cc.getCcUserId());
            if (user != null) {
                vo.setCcUserAvatar(user.getAvatar());
            }
        }

        WorkflowInstance instance = workflowInstanceMapper.selectById(cc.getInstanceId());
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

        return vo;
    }
}
