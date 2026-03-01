package com.admin.module.workflow.service.impl;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.common.security.SecurityUtils;
import com.admin.module.workflow.entity.WorkflowDefinition;
import com.admin.module.workflow.form.WorkflowDefinitionForm;
import com.admin.module.workflow.mapper.WorkflowDefinitionMapper;
import com.admin.module.workflow.query.WorkflowDefinitionQuery;
import com.admin.module.workflow.service.WorkflowDefinitionService;
import com.admin.module.workflow.vo.WorkflowDefinitionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowDefinitionServiceImpl extends ServiceImpl<WorkflowDefinitionMapper, WorkflowDefinition> 
        implements WorkflowDefinitionService {

    private final RepositoryService repositoryService;
    private final WorkflowDefinitionMapper workflowDefinitionMapper;

    @Override
    public PageResult<WorkflowDefinitionVO> selectDefinitionPage(WorkflowDefinitionQuery query) {
        Page<WorkflowDefinition> page = new Page<>(query.getPage(), query.getLimit());
        
        LambdaQueryWrapper<WorkflowDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowDefinition::getDeleted, 0);
        
        if (StringUtils.isNotBlank(query.getProcessKey())) {
            wrapper.like(WorkflowDefinition::getProcessKey, query.getProcessKey());
        }
        if (StringUtils.isNotBlank(query.getProcessName())) {
            wrapper.like(WorkflowDefinition::getProcessName, query.getProcessName());
        }
        if (StringUtils.isNotBlank(query.getCategory())) {
            wrapper.eq(WorkflowDefinition::getCategory, query.getCategory());
        }
        if (query.getStatus() != null) {
            wrapper.eq(WorkflowDefinition::getStatus, query.getStatus());
        }
        
        wrapper.orderByDesc(WorkflowDefinition::getCreateTime);
        
        IPage<WorkflowDefinition> result = this.page(page, wrapper);

        IPage<WorkflowDefinitionVO> voPage = result.convert(this::convertToVO);

        return PageResult.success(voPage);
    }

    @Override
    public WorkflowDefinitionVO getDefinitionById(Long id) {
        WorkflowDefinition definition = this.getById(id);
        if (definition == null) {
            return null;
        }
        return convertToVO(definition);
    }

    @Override
    public WorkflowDefinitionVO getLatestByKey(String processKey) {
        LambdaQueryWrapper<WorkflowDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowDefinition::getProcessKey, processKey)
                .eq(WorkflowDefinition::getStatus, 1)
                .eq(WorkflowDefinition::getDeleted, 0)
                .orderByDesc(WorkflowDefinition::getVersion)
                .last("LIMIT 1");
        
        WorkflowDefinition definition = this.getOne(wrapper);
        if (definition == null) {
            return null;
        }
        return convertToVO(definition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> saveDefinition(WorkflowDefinitionForm form) {
        WorkflowDefinition definition = new WorkflowDefinition();
        BeanUtils.copyProperties(form, definition);
        
        Integer maxVersion = workflowDefinitionMapper.selectMaxVersion(form.getProcessKey());
        definition.setVersion(maxVersion == null ? 1 : maxVersion + 1);
        definition.setStatus(0);
        definition.setTenantId(SecurityUtils.getTenantId());
        
        boolean success = this.save(definition);
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> updateDefinition(WorkflowDefinitionForm form) {
        if (form.getId() == null) {
            return ResultVO.error("流程定义ID不能为空");
        }
        
        WorkflowDefinition existing = this.getById(form.getId());
        if (existing == null) {
            return ResultVO.error("流程定义不存在");
        }
        
        if (existing.getStatus() == 1) {
            return ResultVO.error("已发布的流程不能修改");
        }
        
        WorkflowDefinition definition = new WorkflowDefinition();
        BeanUtils.copyProperties(form, definition);
        
        boolean success = this.updateById(definition);
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> deleteDefinition(Long id) {
        WorkflowDefinition definition = this.getById(id);
        if (definition == null) {
            return ResultVO.error("流程定义不存在");
        }
        
        if (definition.getStatus() == 1) {
            return ResultVO.error("已发布的流程不能删除");
        }
        
        boolean success = this.removeById(id);
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> deployDefinition(Long id) {
        WorkflowDefinition definition = this.getById(id);
        if (definition == null) {
            return ResultVO.error("流程定义不存在");
        }
        
        if (StringUtils.isBlank(definition.getModelXml())) {
            return ResultVO.error("流程模型内容为空");
        }
        
        try {
            String processName = definition.getProcessName() + "_v" + definition.getVersion();
            
            Deployment deployment = repositoryService.createDeployment()
                    .name(processName)
                    .addInputStream(definition.getProcessKey() + ".bpmn20.xml",
                            new ByteArrayInputStream(definition.getModelXml().getBytes(StandardCharsets.UTF_8)))
                    .tenantId(String.valueOf(definition.getTenantId()))
                    .deploy();
            
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();
            
            definition.setDeploymentId(deployment.getId());
            definition.setFlowableDefinitionId(processDefinition.getId());
            definition.setStatus(1);
            this.updateById(definition);
            
            log.info("流程部署成功: {}", processName);
            return ResultVO.success(true);
        } catch (Exception e) {
            log.error("流程部署失败", e);
            return ResultVO.error("流程部署失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> suspendDefinition(Long id) {
        WorkflowDefinition definition = this.getById(id);
        if (definition == null) {
            return ResultVO.error("流程定义不存在");
        }
        
        if (StringUtils.isNotBlank(definition.getFlowableDefinitionId())) {
            repositoryService.suspendProcessDefinitionById(definition.getFlowableDefinitionId());
        }
        
        definition.setStatus(2);
        boolean success = this.updateById(definition);
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> activateDefinition(Long id) {
        WorkflowDefinition definition = this.getById(id);
        if (definition == null) {
            return ResultVO.error("流程定义不存在");
        }
        
        if (StringUtils.isNotBlank(definition.getFlowableDefinitionId())) {
            repositoryService.activateProcessDefinitionById(definition.getFlowableDefinitionId());
        }
        
        definition.setStatus(1);
        boolean success = this.updateById(definition);
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> copyDefinition(Long id) {
        WorkflowDefinition definition = this.getById(id);
        if (definition == null) {
            return ResultVO.error("流程定义不存在");
        }
        
        WorkflowDefinition newDefinition = new WorkflowDefinition();
        BeanUtils.copyProperties(definition, newDefinition);
        newDefinition.setId(null);
        newDefinition.setProcessKey(definition.getProcessKey() + "_copy");
        newDefinition.setProcessName(definition.getProcessName() + "_副本");
        newDefinition.setVersion(1);
        newDefinition.setStatus(0);
        newDefinition.setFlowableDefinitionId(null);
        newDefinition.setDeploymentId(null);
        newDefinition.setCreateTime(null);
        newDefinition.setUpdateTime(null);
        newDefinition.setCreateUser(null);
        newDefinition.setUpdateUser(null);
        
        boolean success = this.save(newDefinition);
        return ResultVO.judge(success);
    }

    @Override
    public List<String> getCategoryList() {
        return baseMapper.selectList(new LambdaQueryWrapper<WorkflowDefinition>()
                        .select(WorkflowDefinition::getCategory)
                        .eq(WorkflowDefinition::getDeleted, 0)
                        .groupBy(WorkflowDefinition::getCategory))
                .stream()
                .map(WorkflowDefinition::getCategory)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    @Override
    public String getProcessXml(Long id) {
        WorkflowDefinition definition = this.getById(id);
        return definition != null ? definition.getModelXml() : null;
    }

    @Override
    public String getProcessDiagram(Long id) {
        WorkflowDefinition definition = this.getById(id);
        return definition != null ? definition.getDiagramSvg() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> importDefinition(String xml, String name, String category) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setProcessKey("imported_" + System.currentTimeMillis());
        definition.setProcessName(name);
        definition.setCategory(category);
        definition.setModelXml(xml);
        definition.setVersion(1);
        definition.setStatus(0);
        definition.setTenantId(SecurityUtils.getTenantId());
        
        boolean success = this.save(definition);
        return ResultVO.judge(success);
    }

    @Override
    public String exportDefinition(Long id) {
        WorkflowDefinition definition = this.getById(id);
        return definition != null ? definition.getModelXml() : null;
    }

    private WorkflowDefinitionVO convertToVO(WorkflowDefinition definition) {
        WorkflowDefinitionVO vo = new WorkflowDefinitionVO();
        BeanUtils.copyProperties(definition, vo);
        
        if (definition.getStatus() != null) {
            vo.setStatusName(getStatusName(definition.getStatus()));
        }
        
        return vo;
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 0: return "草稿";
            case 1: return "已发布";
            case 2: return "已停用";
            default: return "未知";
        }
    }
}
