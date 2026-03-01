package com.admin.module.workflow.mapper;

import com.admin.module.workflow.entity.WorkflowForm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程表单Mapper
 */
@Mapper
public interface WorkflowFormMapper extends BaseMapper<WorkflowForm> {

    /**
     * 根据表单编码查询
     */
    @Select("SELECT * FROM workflow_form WHERE form_code = #{formCode} AND deleted = 0")
    WorkflowForm selectByFormCode(@Param("formCode") String formCode);

    /**
     * 根据分类查询表单列表
     */
    @Select("SELECT * FROM workflow_form WHERE category = #{category} AND status = 1 AND deleted = 0 ORDER BY sort_order")
    List<WorkflowForm> selectByCategory(@Param("category") String category);

    /**
     * 查询已发布的表单
     */
    @Select("SELECT * FROM workflow_form WHERE status = 1 AND deleted = 0 ORDER BY sort_order")
    List<WorkflowForm> selectPublishedForms();
}
