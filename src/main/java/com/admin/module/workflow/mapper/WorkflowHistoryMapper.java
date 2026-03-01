package com.admin.module.workflow.mapper;

import com.admin.module.workflow.entity.WorkflowHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程历史记录Mapper
 */
@Mapper
public interface WorkflowHistoryMapper extends BaseMapper<WorkflowHistory> {

    /**
     * 根据流程实例ID查询历史记录
     */
    @Select("SELECT * FROM workflow_history WHERE instance_id = #{instanceId} AND deleted = 0 ORDER BY operation_time ASC")
    List<WorkflowHistory> selectByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * 根据任务ID查询历史记录
     */
    @Select("SELECT * FROM workflow_history WHERE task_id = #{taskId} AND deleted = 0 ORDER BY operation_time ASC")
    List<WorkflowHistory> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 查询最新的历史记录
     */
    @Select("SELECT * FROM workflow_history WHERE instance_id = #{instanceId} AND deleted = 0 ORDER BY operation_time DESC LIMIT 1")
    WorkflowHistory selectLatestByInstanceId(@Param("instanceId") Long instanceId);
}
