package com.admin.module.workflow.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流程回调记录实体
 * 用于记录流程回调通知
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "workflow_callback")
public class WorkflowCallback extends BaseEntity {

    @TableId(value = "id")
    private Long id;

    /**
     * 流程实例ID
     */
    @TableField(value = "instance_id")
    private Long instanceId;

    /**
     * 业务类型
     */
    @TableField(value = "business_type")
    private String businessType;

    /**
     * 业务Key
     */
    @TableField(value = "business_key")
    private String businessKey;

    /**
     * 回调类型（1:流程开始 2:流程结束 3:节点进入 4:节点完成 5:任务分配 6:审批结果）
     */
    @TableField(value = "callback_type")
    private Integer callbackType;

    /**
     * 回调URL
     */
    @TableField(value = "callback_url")
    private String callbackUrl;

    /**
     * 回调数据JSON
     */
    @TableField(value = "callback_data")
    private String callbackData;

    /**
     * 回调状态（0:待发送 1:发送成功 2:发送失败）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 发送次数
     */
    @TableField(value = "retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField(value = "max_retry")
    private Integer maxRetry;

    /**
     * 响应结果
     */
    @TableField(value = "response_result")
    private String responseResult;

    /**
     * 发送时间
     */
    @TableField(value = "send_time")
    private LocalDateTime sendTime;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id")
    private Long tenantId;
}
