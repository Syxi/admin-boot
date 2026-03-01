package com.admin.module.workflow.mapper;

import com.admin.module.workflow.entity.WorkflowNodeConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程节点配置Mapper
 */
@Mapper
public interface WorkflowNodeConfigMapper extends BaseMapper<WorkflowNodeConfig> {

    /**
     * 根据流程定义ID查询节点配置
     */
    @Select("SELECT * FROM workflow_node_config WHERE definition_id = #{definitionId} AND deleted = 0 ORDER BY sort_order")
    List<WorkflowNodeConfig> selectByDefinitionId(@Param("definitionId") Long definitionId);

    /**
     * 根据流程定义ID和节点ID查询
     */
    @Select("SELECT * FROM workflow_node_config WHERE definition_id = #{definitionId} AND node_id = #{nodeId} AND deleted = 0")
    WorkflowNodeConfig selectByNodeId(@Param("definitionId") Long definitionId, @Param("nodeId") String nodeId);

    /**
     * 根据节点类型查询
     */
    @Select("SELECT * FROM workflow_node_config WHERE definition_id = #{definitionId} AND node_type = #{nodeType} AND deleted = 0")
    List<WorkflowNodeConfig> selectByNodeType(@Param("definitionId") Long definitionId, @Param("nodeType") String nodeType);
}
