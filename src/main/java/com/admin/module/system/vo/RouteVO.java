package com.admin.module.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 菜单路由
 * @Author: suYan
 * @Date: 2023-11-29
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouteVO {

    // 菜单id
//    private Long menuId;

    // 路由路径
    private String path;

    // 组件路径
    private String component;

    // 跳转链接
    private String redirect;

    // 路由名称
    private String name;

    // 路由属性
    private Meta meta;

    //路由属性类型
    @Data
    public static class Meta {

        // 路由title
        private String title;

        // icon
        private String icon;

        // 是否隐藏
        private Boolean hideInMenu;

        // 拥有路由权限的角色编码
//        private Set<String> roles;

        // 菜单是否开启页面缓存
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Boolean keepAlive;

        // 目录只有一个子路由是否始终显示
//        @JsonInclude(JsonInclude.Include.NON_NULL)
//        private Boolean alwaysShow;

        // 路由参数
        private Map<String, String> query;
    }

    // 子路由列表
    private List<RouteVO> children;
}
