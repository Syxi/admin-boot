package com.admin.module.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.admin.common.enums.MenuTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author suYan
 * @date 2023/4/2 20:51
 */
@Schema(description = "菜单视图对象")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MenuVO {

    @Schema(description = "菜单ID")
    private Long menuId;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单名称")
    private String menuName;

    @Schema(description = "菜单类型(0:目录；1:菜单；2按钮)")
    private MenuTypeEnum menuType;

    @Schema(description = "路由名称")
    private String routeName;

    @Schema(description = "路由路径")
    private String routePath;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "跳转路径")
    private String redirect;

    @Schema(description = "菜单排序(数字越小排名越靠前)")
    private Integer sort;

    @Schema(description = "菜单是否可见(1:显示;-1:隐藏)")
    private Integer status;

    @Schema(description = "按钮权限标识")
    private String perm;

    @Schema(description = "子菜单")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private List<MenuVO> children;







}
