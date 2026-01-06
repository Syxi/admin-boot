package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户套餐查询对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantPackageQuery extends BasePage {

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "套餐编码")
    private String code;

    @Schema(description = "状态")
    private Integer status;
}