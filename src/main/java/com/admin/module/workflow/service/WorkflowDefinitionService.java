package com.admin.module.workflow.service;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.workflow.entity.WorkflowDefinition;
import com.admin.module.workflow.vo.WorkflowDefinitionVO;
import com.admin.module.workflow.form.WorkflowDefinitionForm;
import com.admin.module.workflow.query.WorkflowDefinitionQuery;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 流程定义服务接口
 */
public interface WorkflowDefinitionService extends IService<WorkflowDefinition> {

    /**
     * 分页查询流程定义
     */
    PageResult<WorkflowDefinitionVO> selectDefinitionPage(WorkflowDefinitionQuery query);

    /**
     * 获取流程定义详情
     */
    WorkflowDefinitionVO getDefinitionById(Long id);

    /**
     * 根据流程Key获取最新版本
     */
    WorkflowDefinitionVO getLatestByKey(String processKey);

    /**
     * 保存流程定义
     */
    ResultVO<Boolean> saveDefinition(WorkflowDefinitionForm form);

    /**
     * 更新流程定义
     */
    ResultVO<Boolean> updateDefinition(WorkflowDefinitionForm form);

    /**
     * 删除流程定义
     */
    ResultVO<Boolean> deleteDefinition(Long id);

    /**
     * 发布流程定义
     */
    ResultVO<Boolean> deployDefinition(Long id);

    /**
     * 停用流程定义
     */
    ResultVO<Boolean> suspendDefinition(Long id);

    /**
     * 激活流程定义
     */
    ResultVO<Boolean> activateDefinition(Long id);

    /**
     * 复制流程定义
     */
    ResultVO<Boolean> copyDefinition(Long id);

    /**
     * 获取流程分类列表
     */
    List<String> getCategoryList();

    /**
     * 获取流程定义XML
     */
    String getProcessXml(Long id);

    /**
     * 获取流程图SVG
     */
    String getProcessDiagram(Long id);

    /**
     * 导入流程定义
     */
    ResultVO<Boolean> importDefinition(String xml, String name, String category);

    /**
     * 导出流程定义
     */
    String exportDefinition(Long id);
}
