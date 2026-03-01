package com.admin.module.workflow.mapper;

import com.admin.module.workflow.entity.WorkflowDefinition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 流程定义Mapper
 */
@Mapper
public interface WorkflowDefinitionMapper extends BaseMapper<WorkflowDefinition> {

    /**
     * 根据流程Key和版本查询
     */
    @Select("SELECT * FROM workflow_definition WHERE process_key = #{processKey} AND version = #{version} AND deleted = 0")
    WorkflowDefinition selectByKeyAndVersion(@Param("processKey") String processKey, @Param("version") Integer version);

    /**
     * 获取流程Key的最大版本
     */
    @Select("SELECT MAX(version) FROM workflow_definition WHERE process_key = #{processKey} AND deleted = 0")
    Integer selectMaxVersion(@Param("processKey") String processKey);

    /**
     * 根据分类查询流程定义列表
     */
    @Select("SELECT * FROM workflow_definition WHERE category = #{category} AND status = 1 AND deleted = 0 ORDER BY sort_order")
    List<WorkflowDefinition> selectByCategory(@Param("category") String category);

    /**
     * 更新流程状态
     */
    @Update("UPDATE workflow_definition SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据Flowable定义ID查询
     */
    @Select("SELECT * FROM workflow_definition WHERE flowable_definition_id = #{definitionId} AND deleted = 0")
    WorkflowDefinition selectByFlowableDefinitionId(@Param("definitionId") String definitionId);
}
