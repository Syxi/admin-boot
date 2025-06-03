package com.admin.module.system.vo;

import lombok.Data;

@Data
public class SysSwitchConfigVO {

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
    private Boolean configValue;


    private String remark;
}
