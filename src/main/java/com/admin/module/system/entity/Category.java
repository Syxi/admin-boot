package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文章分类表
 * @TableName t_category
 */
@TableName(value ="t_category")
@Data
public class Category extends BaseEntity {
    /**
     * 
     */
    @TableId
    private Long categoryId;


    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 文章量
     */
    private Integer articleCount;

    /**
     * 排序
     */
    private Integer sort;

}