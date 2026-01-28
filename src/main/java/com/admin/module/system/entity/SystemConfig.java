package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统配置表
 * @TableName system_config
 */
@TableName(value ="system_config")
@Data
public class SystemConfig extends BaseEntity {
    /**
     * 主键
     */
    @TableId
    private Long id;


    /**
     * 配置名称
     */
    private String configName;

    /**
     * key
     */
    private String configKey;

    /**
     * value
     */
    private String configValue;

    /**
     * 状态（1=启用，-1=禁用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}