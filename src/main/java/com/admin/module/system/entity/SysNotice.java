package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告表
 * @TableName sys_notice
 */
@TableName(value ="sys_notice")
@Data
public class SysNotice extends BaseEntity {
    /**
     * 
     */
    @TableId
    private Long noticeId;


    /**
     * 通知标题
     */
    private String noticeTitle;

    /**
     * 通知类型
     */
    private Integer noticeType;

    /**
     * 通知内容
     */
    private String noticeContent;

    /**
     * 通知状态 (1：发布，-1：未发布)
     */
    private Integer isPublish;

    /**
     * 置顶状态 (1：置顶，-1：未置顶)
     */
    private Integer isTop;

    /**
     * 置顶时间
     */
    private LocalDateTime topTime;

    /**
     * 备注
     */
    private String remark;

}