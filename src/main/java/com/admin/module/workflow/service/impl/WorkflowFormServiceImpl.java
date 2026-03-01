package com.admin.module.workflow.service.impl;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.common.security.SecurityUtils;
import com.admin.module.workflow.entity.WorkflowForm;
import com.admin.module.workflow.form.WorkflowFormForm;
import com.admin.module.workflow.mapper.WorkflowFormMapper;
import com.admin.module.workflow.service.WorkflowFormService;
import com.admin.module.workflow.vo.WorkflowFormVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程表单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowFormServiceImpl extends ServiceImpl<WorkflowFormMapper, WorkflowForm>
        implements WorkflowFormService {

    private final WorkflowFormMapper workflowFormMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResult<WorkflowFormVO> selectFormPage(String formName, String category, Integer status, Integer pageNum, Integer pageSize) {
        Page<WorkflowForm> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<WorkflowForm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowForm::getDeleted, 0);

        if (StringUtils.isNotBlank(formName)) {
            wrapper.like(WorkflowForm::getFormName, formName);
        }
        if (StringUtils.isNotBlank(category)) {
            wrapper.eq(WorkflowForm::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(WorkflowForm::getStatus, status);
        }

        wrapper.orderByDesc(WorkflowForm::getCreateTime);

        IPage<WorkflowForm> result = this.page(page, wrapper);

        IPage<WorkflowFormVO> voPage = result.convert(this::convertToVO);

        return PageResult.success(voPage);
    }

    @Override
    public WorkflowFormVO getFormById(Long id) {
        WorkflowForm form = this.getById(id);
        if (form == null) {
            return null;
        }
        return convertToVO(form);
    }

    @Override
    public WorkflowFormVO getFormByCode(String formCode) {
        WorkflowForm form = workflowFormMapper.selectByFormCode(formCode);
        if (form == null) {
            return null;
        }
        return convertToVO(form);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> saveForm(WorkflowFormForm form) {
        WorkflowForm entity = new WorkflowForm();
        BeanUtils.copyProperties(form, entity);
        entity.setStatus(0);
        entity.setVersion(1);
        entity.setTenantId(SecurityUtils.getTenantId());

        boolean success = this.save(entity);
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> updateForm(WorkflowFormForm form) {
        if (form.getId() == null) {
            return ResultVO.error("表单ID不能为空");
        }

        WorkflowForm existing = this.getById(form.getId());
        if (existing == null) {
            return ResultVO.error("表单不存在");
        }

        if (existing.getStatus() == 1) {
            return ResultVO.error("已发布的表单不能修改");
        }

        WorkflowForm entity = new WorkflowForm();
        BeanUtils.copyProperties(form, entity);

        boolean success = this.updateById(entity);
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> deleteForm(Long id) {
        WorkflowForm form = this.getById(id);
        if (form == null) {
            return ResultVO.error("表单不存在");
        }

        if (form.getStatus() == 1) {
            return ResultVO.error("已发布的表单不能删除");
        }

        boolean success = this.removeById(id);
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> publishForm(Long id) {
        WorkflowForm form = this.getById(id);
        if (form == null) {
            return ResultVO.error("表单不存在");
        }

        form.setStatus(1);
        boolean success = this.updateById(form);
        return ResultVO.judge(success);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<Boolean> disableForm(Long id) {
        WorkflowForm form = this.getById(id);
        if (form == null) {
            return ResultVO.error("表单不存在");
        }

        form.setStatus(2);
        boolean success = this.updateById(form);
        return ResultVO.judge(success);
    }

    @Override
    public List<String> getCategoryList() {
        return baseMapper.selectList(new LambdaQueryWrapper<WorkflowForm>()
                        .select(WorkflowForm::getCategory)
                        .eq(WorkflowForm::getDeleted, 0)
                        .groupBy(WorkflowForm::getCategory))
                .stream()
                .map(WorkflowForm::getCategory)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getFormFields(Long formId) {
        WorkflowForm form = this.getById(formId);
        if (form == null || form.getFieldConfig() == null) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(form.getFieldConfig(), new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public ResultVO<Boolean> validateFormData(Long formId, Map<String, Object> formData) {
        List<Map<String, Object>> fields = getFormFields(formId);

        for (Map<String, Object> field : fields) {
            String fieldName = (String) field.get("name");
            Boolean required = (Boolean) field.get("required");

            if (Boolean.TRUE.equals(required)) {
                if (formData == null || !formData.containsKey(fieldName) || formData.get(fieldName) == null) {
                    String label = (String) field.get("label");
                    return ResultVO.error(label + "不能为空");
                }
            }
        }

        return ResultVO.success(true);
    }

    @Override
    public List<WorkflowFormVO> getPublishedForms() {
        List<WorkflowForm> forms = workflowFormMapper.selectPublishedForms();
        return forms.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private WorkflowFormVO convertToVO(WorkflowForm form) {
        WorkflowFormVO vo = new WorkflowFormVO();
        BeanUtils.copyProperties(form, vo);

        if (form.getStatus() != null) {
            vo.setStatusName(getStatusName(form.getStatus()));
        }

        if (form.getFormConfig() != null) {
            try {
                vo.setFormConfig(objectMapper.readValue(form.getFormConfig(), new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败", e);
            }
        }
        if (form.getFieldConfig() != null) {
            try {
                vo.setFieldConfig(objectMapper.readValue(form.getFieldConfig(), new TypeReference<List<Map<String, Object>>>() {}));
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败", e);
            }
        }

        return vo;
    }

    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已发布";
            case 2 -> "已停用";
            default -> "未知";
        };
    }
}
