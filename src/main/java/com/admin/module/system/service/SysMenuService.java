package com.admin.module.system.service;

import com.admin.module.system.entity.SysMenu;
import com.admin.module.system.form.MenuForm;
import com.admin.module.system.query.MenuQuery;
import com.admin.module.system.vo.MenuVO;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.vo.RouteVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 菜单列表
     * @param menuQuery
     * @return
     */
    List<MenuVO> menuTree(MenuQuery menuQuery);



    /**
     * 菜单下拉列表
     * @return
     */
    List<OptionVO> menuTreeOption();


    /**
     * 路由列表
     * @return
     */
    List<RouteVO> selectRouteList(Set<String> roleCodes);


    /**
     * 新增菜单
     * @param menuForm
     * @return
     */
    boolean saveMenu(MenuForm menuForm);

    /**
     * 编辑菜单
     * @param menuForm
     * @return
     */
    boolean updateMenu(MenuForm menuForm);


    /**
     * 获取菜单详情，(编辑菜单)
     * @param menuId
     * @return
     */
    MenuForm getMenuDetail(Long menuId);



    /**
     * 删除菜单
     * @param menuId
     * @return
     */
    boolean deleteMenu(Long menuId);



    /**
     * 通过menuIds 获取菜单权限
     * @param menuIds
     * @return
     */
    Set<String> selectMenuPerms(List<Long> menuIds);



    /**
     * 更新角色的菜单资源
     * @param roleId
     * @param menuIds
     * @return
     */
    boolean updateRoleMenuList(Long roleId, List<Long> menuIds);


    /**
     * 初始化全部角色缓存
     */
    void refreshRolePermsCache();


    /**
     * 更新指定角色缓存
     */
    void refreshRolePermsCache(Long roleId);

}
