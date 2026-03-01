package com.admin.module.workflow.mapper;

import com.admin.module.workflow.entity.WorkflowCallback;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 流程回调记录Mapper
 */
@Mapper
public interface WorkflowCallbackMapper extends BaseMapper<WorkflowCallback> {

    /**
     * 查询待发送的回调记录
     */
    @Select("SELECT * FROM workflow_callback WHERE status = 0 AND retry_count < max_retry AND deleted = 0 ORDER BY create_time ASC LIMIT 100")
    List<WorkflowCallback> selectPendingCallbacks();

    /**
     * 根据流程实例ID查询回调记录
     */
    @Select("SELECT * FROM workflow_callback WHERE instance_id = #{instanceId} AND deleted = 0 ORDER BY create_time DESC")
    List<WorkflowCallback> selectByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * 更新回调状态
     */
    @Update("UPDATE workflow_callback SET status = #{status}, retry_count = retry_count + 1, " +
            "response_result = #{responseResult}, send_time = NOW() WHERE id = #{id}")
    int updateCallbackStatus(@Param("id") Long id, @Param("status") Integer status, @Param("responseResult") String responseResult);
}
