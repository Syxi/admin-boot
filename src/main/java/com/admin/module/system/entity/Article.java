package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文章表
 * @TableName t_article
 */
@TableName(value ="t_article")
@Data
public class Article extends BaseEntity {
    /**
     * 
     */
    @TableId
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

}