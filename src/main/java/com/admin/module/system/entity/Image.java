package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图片表
 * @TableName t_image
 */
@TableName(value ="t_image")
@Data
public class Image implements Serializable {
    /**
     * 图片id
     */
    @TableId
    private Long id;

    /**
     * 图片名称
     */
    private String imageName;

    /**
     * SHA-256散列值，图片名称的唯一标识
     */
    private String imageMd5;

    /**
     * 图片大小
     */
    private String imageSize;

    /**
     * 图片类型
     */
    private String imageType;

    /**
     * 图片访问url地址
     */
    private String url;

    /**
     * 图片存储路径
     */
    private String storagePath;

    /**
     * 图片描述/标签
     */
    private String description;

    /**
     * 文件上传时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime createTime;

    /**
     * 图片更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime updateTime;

    /**
     * 上传者id
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long createUser;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}