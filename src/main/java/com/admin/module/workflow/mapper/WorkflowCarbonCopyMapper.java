package com.admin.module.workflow.mapper;

import com.admin.module.workflow.entity.WorkflowCarbonCopy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 流程抄送记录Mapper
 */
@Mapper
public interface WorkflowCarbonCopyMapper extends BaseMapper<WorkflowCarbonCopy> {

    /**
     * 分页查询抄送记录
     */
    IPage<WorkflowCarbonCopy> selectCcPage(Page<WorkflowCarbonCopy> page,
            @Param("ccUserId") Long ccUserId,
            @Param("isRead") Integer isRead);

    /**
     * 根据流程实例ID查询抄送记录
     */
    @Select("SELECT * FROM workflow_carbon_copy WHERE instance_id = #{instanceId} AND deleted = 0 ORDER BY cc_time DESC")
    List<WorkflowCarbonCopy> selectByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * 根据用户ID查询未读抄送数量
     */
    @Select("SELECT COUNT(*) FROM workflow_carbon_copy WHERE cc_user_id = #{ccUserId} AND is_read = 0 AND deleted = 0")
    Long countUnreadByUserId(@Param("ccUserId") Long ccUserId);

    /**
     * 标记已读
     */
    @Update("UPDATE workflow_carbon_copy SET is_read = 1, read_time = NOW() WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    /**
     * 批量标记已读
     */
    @Update("UPDATE workflow_carbon_copy SET is_read = 1, read_time = NOW() WHERE cc_user_id = #{ccUserId} AND is_read = 0")
    int markAllAsRead(@Param("ccUserId") Long ccUserId);
}
