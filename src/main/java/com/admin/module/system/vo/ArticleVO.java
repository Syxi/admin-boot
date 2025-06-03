package com.admin.module.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleVO {

    private Long articleId;

    /**
     * 文章分类
     */
    private String categoryName;


    /**
     * 文章标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 文章封面图片
     */
    private String avatar;

    /**
     * 文章简介
     */
    private String introduction;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章阅读量
     */
    private Integer readCount;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否发布  1：发布， -1: 未发布
     */
    private Integer publish;

    /**
     * 是否置顶  1：是， -1：否
     */
    private Integer top;


    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime createTime;

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime updateTime;

}
