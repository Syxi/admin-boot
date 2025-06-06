package com.admin.module.system.dto;

import com.admin.common.enums.MenuTypeEnum;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author suYan
 * @date 2023/12/2 11:23
 */
@Data
public class RouteDTO {

    private Long menuId;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型(1-菜单；2-目录；3-外链；4-按钮权限)
     */
    private MenuTypeEnum menuType;

    /**
     * 路由路径(浏览器地址栏路径)
     */
    private String path;

    /**
     * 路由参数
     */

    private String params;

    /**
     * 组件路径(vue页面完整路径，省略.vue后缀)
     */
    private String component;

    /**
     * 权限标识
     */
    private String perm;

    /**
     * 显示状态(1:显示;-1:隐藏)
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 跳转路径
     */
    private String redirect;

    /**
     * 拥有路由的权限
     */
    private Set<String> roleCodes;

    /**
     * 【目录】只有一个子路由是否始终显示(1:是 -1:否)
     */
    private Integer alwaysShow;

    /**
     * 【菜单】是否开启页面缓存(1:是 -1:否)
     */
    private Integer keepAlive;


    private List<RouteDTO> children;
}
