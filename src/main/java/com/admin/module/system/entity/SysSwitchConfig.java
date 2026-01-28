package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 系统业务开关配置表
 * @TableName sys_switch_config
 */
@TableName(value ="sys_switch_config")
@Data
public class SysSwitchConfig extends BaseEntity {
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
     * value （true=启用，false=禁用）
     */
    private String configValue;


    /**
     * 备注
     */
    private String remark;


}