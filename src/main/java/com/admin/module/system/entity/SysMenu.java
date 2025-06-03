package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.admin.common.enums.MenuTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
    * 菜单管理
    */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_menu")
public class SysMenu {

    private static final Long serialVersionUID = 1L;

    @TableId(value = "menu_id")
    private Long menuId;

    /**
     * 父菜单ID
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 父节点id路径
     */
    @TableField(value = "tree_path")
    private String treePath;

    /**
     * 菜单名称
     */
    @TableField(value = "menu_name")
    private String menuName;

    /**
     * 菜单类型(0:目录；1:菜单；2按钮)
     */
    @TableField(value = "menu_type")
    private MenuTypeEnum menuType;


    /**
     * 路由参数
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String params;

    /**
     * 路由名称
     */
    @TableField(value = "route_name")
    private String routeName;

    /**
     * 路由路径
     */
    @TableField(value = "route_path")
    private String routePath;

    /**
     * 组件路径
     */
    @TableField(value = "component")
    private String component;

    /**
     * 跳转路径
     */
    @TableField(value = "redirect")
    private String redirect;

    /**
     * 权限标识
     */
    @TableField(value = "perm")
    private String perm;

    /**
     * 显示状态((1:正常;-1:禁用))
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 菜单图标
     */
    @TableField(value = "icon")
    private String icon;


    /**
     * 目录只有一个子路由是否始终显示(1:是，-1：否)
     */
    @TableField(value = "always_show")
    private Integer alwaysShow;

    /**
     * 菜单是否开启页面缓存(1:是，-1：否)
     */
    @TableField(value = "keep_alive")
    private Integer keepAlive;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;


    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Long updateUser;

    /**
     * 逻辑删除标识(0-未删除；1-已删除)
     */
    @TableLogic
    private Integer deleted;






}