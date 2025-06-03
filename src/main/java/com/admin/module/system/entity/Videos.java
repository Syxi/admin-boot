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
 * 视频文件记录表
 * @TableName t_videos
 */
@TableName(value ="t_videos")
@Data
public class Videos implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 视频名称
     */
    private String fileName;

    /**
     * 视频的描述
     */
    private String description;

    /**
     * 视频文件的存储路径
     */
    private String filePath;

    /**
     * 备注
     */
    private String remark;


    /**
     * 文件上传时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime createTime;

    /**
     * 文件更新时间
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