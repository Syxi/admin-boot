package com.admin.module.system.vo;

import com.admin.common.base.BasePage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlatformVO extends BasePage {
    private Long id;

    /**
     * 平台名称
     */
    private String name;

    /**
     * 平台名称
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
