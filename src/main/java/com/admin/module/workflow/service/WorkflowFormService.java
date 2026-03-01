package com.admin.module.workflow.service;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.workflow.entity.WorkflowForm;
import com.admin.module.workflow.vo.WorkflowFormVO;
import com.admin.module.workflow.form.WorkflowFormForm;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 流程表单服务接口
 */
public interface WorkflowFormService extends IService<WorkflowForm> {

    /**
     * 分页查询表单
     */
    PageResult<WorkflowFormVO> selectFormPage(String formName, String category, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取表单详情
     */
    WorkflowFormVO getFormById(Long id);

    /**
     * 根据表单编码获取
     */
    WorkflowFormVO getFormByCode(String formCode);

    /**
     * 保存表单
     */
    ResultVO<Boolean> saveForm(WorkflowFormForm form);

    /**
     * 更新表单
     */
    ResultVO<Boolean> updateForm(WorkflowFormForm form);

    /**
     * 删除表单
     */
    ResultVO<Boolean> deleteForm(Long id);

    /**
     * 发布表单
     */
    ResultVO<Boolean> publishForm(Long id);

    /**
     * 停用表单
     */
    ResultVO<Boolean> disableForm(Long id);

    /**
     * 获取表单分类列表
     */
    List<String> getCategoryList();

    /**
     * 获取表单字段配置
     */
    List<Map<String, Object>> getFormFields(Long formId);

    /**
     * 验证表单数据
     */
    ResultVO<Boolean> validateFormData(Long formId, Map<String, Object> formData);

    /**
     * 获取已发布的表单列表
     */
    List<WorkflowFormVO> getPublishedForms();
}
