package com.admin.module.system.form;

import com.admin.common.enums.MenuTypeEnum;
import com.admin.module.system.vo.KeyValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author suYan
 * @date 2023/4/2 20:59
 */
@Schema(description = "菜单对象")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MenuForm {

    private Long menuId;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 父节点id路径
     */
    private String treePath;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型(0:目录；1:菜单；2按钮)
     */
    private MenuTypeEnum menuType;

    /**
     * 路由名称
     */
    private String routeName;


    /**
     * 路由路径
     */
    private String routePath;

    /**
     * 路由参数
     */
    private List<KeyValueVO> params;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 跳转路径
     */
    private String redirect;

    /**
     * 权限标识
     */
    private String perm;

    /**
     * 显示状态((1:正常;-1:禁用))
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
     * 目录只有一个子路由是否始终显示(1:是，-1：否)
     */
    private Integer alwaysShow;

    /**
     * 菜单是否开启页面缓存(1:是，-1：否)
     */
    private Integer keepAlive;






}
