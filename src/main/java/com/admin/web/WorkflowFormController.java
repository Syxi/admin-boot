package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.workflow.form.WorkflowFormForm;
import com.admin.module.workflow.service.WorkflowFormService;
import com.admin.module.workflow.vo.WorkflowFormVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程表单控制器
 */
@Slf4j
@Tag(name = "流程表单接口")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/form")
public class WorkflowFormController {

    private final WorkflowFormService workflowFormService;

    @Operation(summary = "表单分页列表")
    @GetMapping("/page")
    public PageResult<WorkflowFormVO> page(
            @RequestParam(required = false) String formName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        return workflowFormService.selectFormPage(formName, category, status, page, limit);
    }

    @Operation(summary = "表单详情")
    @GetMapping("/{id}")
    public ResultVO<WorkflowFormVO> getById(@PathVariable Long id) {
        WorkflowFormVO vo = workflowFormService.getFormById(id);
        return ResultVO.success(vo);
    }

    @Operation(summary = "根据编码获取表单")
    @GetMapping("/code/{formCode}")
    public ResultVO<WorkflowFormVO> getByCode(@PathVariable String formCode) {
        WorkflowFormVO vo = workflowFormService.getFormByCode(formCode);
        return ResultVO.success(vo);
    }

    @Operation(summary = "新增表单")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:form:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> add(@Valid @RequestBody WorkflowFormForm form) {
        return workflowFormService.saveForm(form);
    }

    @Operation(summary = "更新表单")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:form:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> edit(@Valid @RequestBody WorkflowFormForm form) {
        return workflowFormService.updateForm(form);
    }

    @Operation(summary = "删除表单")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:form:delete')")
    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> delete(@PathVariable Long id) {
        return workflowFormService.deleteForm(id);
    }

    @Operation(summary = "发布表单")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:form:publish')")
    @PostMapping("/publish/{id}")
    public ResultVO<Boolean> publish(@PathVariable Long id) {
        return workflowFormService.publishForm(id);
    }

    @Operation(summary = "停用表单")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('workflow:form:disable')")
    @PostMapping("/disable/{id}")
    public ResultVO<Boolean> disable(@PathVariable Long id) {
        return workflowFormService.disableForm(id);
    }

    @Operation(summary = "获取表单分类列表")
    @GetMapping("/categories")
    public ResultVO<List<String>> getCategories() {
        return ResultVO.success(workflowFormService.getCategoryList());
    }

    @Operation(summary = "获取表单字段")
    @GetMapping("/fields/{formId}")
    public ResultVO<List<Map<String, Object>>> getFields(@PathVariable Long formId) {
        List<Map<String, Object>> fields = workflowFormService.getFormFields(formId);
        return ResultVO.success(fields);
    }

    @Operation(summary = "验证表单数据")
    @PostMapping("/validate/{formId}")
    public ResultVO<Boolean> validate(@PathVariable Long formId, @RequestBody Map<String, Object> formData) {
        return workflowFormService.validateFormData(formId, formData);
    }

    @Operation(summary = "获取已发布的表单列表")
    @GetMapping("/published")
    public ResultVO<List<WorkflowFormVO>> getPublishedForms() {
        return ResultVO.success(workflowFormService.getPublishedForms());
    }
}
