package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 笔记表
 * @TableName t_note
 */
@TableName(value ="t_note")
@Data
public class Note extends BaseEntity {
    /**
     * 笔记ID
     */
    @TableId
    private Long noteId;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 状态：1-正常，-1-禁用
     */
    private Integer status;
}
