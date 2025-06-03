package com.admin.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeVO {

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;

}
