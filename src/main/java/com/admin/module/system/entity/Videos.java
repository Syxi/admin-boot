package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 视频文件记录表
 * @TableName t_videos
 */
@TableName(value ="t_videos")
@Data
public class Videos extends BaseEntity {
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}