package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author suYan
 * @date 2023/4/4 15:10
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "权限查询对象")
@Data
public class PermissionQuery extends BasePage {

    @Schema(description = "菜单id")
    private Long menuId;

    @Schema(description = "权限名称")
    private String permissionName;

}
