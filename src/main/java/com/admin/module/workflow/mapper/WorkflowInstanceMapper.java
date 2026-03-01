package com.admin.module.workflow.mapper;

import com.admin.module.workflow.entity.WorkflowInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 流程实例Mapper
 */
@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {

    /**
     * 分页查询流程实例
     */
    IPage<WorkflowInstance> selectInstancePage(Page<WorkflowInstance> page, 
            @Param("definitionId") Long definitionId,
            @Param("applicantId") Long applicantId,
            @Param("status") Integer status,
            @Param("businessType") String businessType,
            @Param("businessTitle") String businessTitle,
            @Param("tenantId") Long tenantId);

    /**
     * 根据流程实例ID查询
     */
    @Select("SELECT * FROM workflow_instance WHERE process_instance_id = #{processInstanceId} AND deleted = 0")
    WorkflowInstance selectByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * 根据业务Key查询
     */
    @Select("SELECT * FROM workflow_instance WHERE business_key = #{businessKey} AND deleted = 0")
    WorkflowInstance selectByBusinessKey(@Param("businessKey") String businessKey);

    /**
     * 查询用户的流程实例
     */
    @Select("SELECT * FROM workflow_instance WHERE applicant_id = #{applicantId} AND deleted = 0 ORDER BY create_time DESC")
    List<WorkflowInstance> selectByApplicantId(@Param("applicantId") Long applicantId);

    /**
     * 更新流程状态
     */
    @Update("UPDATE workflow_instance SET status = #{status}, result = #{result} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("result") Integer result);

    /**
     * 更新当前节点信息
     */
    @Update("UPDATE workflow_instance SET current_node_id = #{nodeId}, current_node_name = #{nodeName}, " +
            "current_assignee_id = #{assigneeId}, current_assignee_name = #{assigneeName} WHERE id = #{id}")
    int updateCurrentNode(@Param("id") Long id, @Param("nodeId") String nodeId, 
                          @Param("nodeName") String nodeName, @Param("assigneeId") Long assigneeId,
                          @Param("assigneeName") String assigneeName);
}
