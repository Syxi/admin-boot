package com.admin.module.workflow.mapper;

import com.admin.module.workflow.entity.WorkflowTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 流程任务Mapper
 */
@Mapper
public interface WorkflowTaskMapper extends BaseMapper<WorkflowTask> {

    /**
     * 分页查询待办任务
     */
    IPage<WorkflowTask> selectTodoPage(Page<WorkflowTask> page,
            @Param("assigneeId") Long assigneeId,
            @Param("candidateGroups") List<String> candidateGroups,
            @Param("processName") String processName,
            @Param("businessTitle") String businessTitle);

    /**
     * 分页查询已办任务
     */
    IPage<WorkflowTask> selectDonePage(Page<WorkflowTask> page,
            @Param("assigneeId") Long assigneeId,
            @Param("processName") String processName,
            @Param("businessTitle") String businessTitle);

    /**
     * 根据Flowable任务ID查询
     */
    @Select("SELECT * FROM workflow_task WHERE task_id = #{taskId} AND deleted = 0")
    WorkflowTask selectByTaskId(@Param("taskId") String taskId);

    /**
     * 根据流程实例ID查询任务列表
     */
    @Select("SELECT * FROM workflow_task WHERE instance_id = #{instanceId} AND deleted = 0 ORDER BY create_time DESC")
    List<WorkflowTask> selectByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * 查询用户的待办任务数量
     */
    @Select("SELECT COUNT(*) FROM workflow_task WHERE assignee_id = #{assigneeId} AND status = 0 AND deleted = 0")
    Long countTodoByUserId(@Param("assigneeId") Long assigneeId);

    /**
     * 更新任务状态
     */
    @Update("UPDATE workflow_task SET status = #{status}, result = #{result}, comment = #{comment}, " +
            "handle_time = NOW(), duration = #{duration} WHERE id = #{id}")
    int updateTaskStatus(@Param("id") Long id, @Param("status") Integer status, 
                         @Param("result") Integer result, @Param("comment") String comment,
                         @Param("duration") Long duration);

    /**
     * 标记任务已读
     */
    @Update("UPDATE workflow_task SET is_read = 1, read_time = NOW() WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);
}
