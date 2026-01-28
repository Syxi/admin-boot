package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 平台系统表
 * @TableName t_platform
 */
@TableName(value ="t_platform")
@Data
public class Platform extends BaseEntity {
    /**
     * 主键
     */
    @TableId
    private Long id;


    /**
     * 平台名称
     */
    private String name;

    /**
     * 平台路由路径
     */
    private String path;

    /**
     * 平台图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 1启用，-1禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remake;
}