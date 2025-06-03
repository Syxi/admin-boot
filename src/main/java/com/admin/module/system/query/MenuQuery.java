package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author suYan
 * @date 2023/4/4 15:09
 */
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单查询对象")
@Data
public class MenuQuery extends BasePage {

    @Schema(description = "菜单名称")
    private String menuName;


}
