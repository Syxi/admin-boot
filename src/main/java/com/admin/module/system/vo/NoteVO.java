package com.admin.module.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteVO {

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

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime createTime;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime updateTime;
}
