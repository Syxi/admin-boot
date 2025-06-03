package com.admin.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.SysRoleMenu;

import java.util.List;
import java.util.Set;

public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 获取角色的菜单id集合
     * @param roleId
     * @return
     */
    List<Long> selectMenuIds(Long roleId);



    /**
     * 获取 menuIds
     * @param roleIds
     * @return
     */
    List<Long> selectMenuIds(Set<Long> roleIds);




    /**
     * 通过menuIds, 获取RoleMenu列表
     * @param menuIds
     * @return
     */
    List<SysRoleMenu> selectRoleMenuList(List<Long> menuIds);

    /**
     * 通过roleIds, 获取RoleMenu列表
     * @param roleIds
     * @return
     */
    List<SysRoleMenu> selectRoleMenus(List<Long> roleIds);


    /**
     * 删除角色菜单的关联
     * @param roleId
     */
    void deleteRoleMenu(Long roleId);

    /**
     * 批量删除角色菜单的关联
     * @param roleIds
     */
    void deleteBatchRoleMenu(List<Long> roleIds);




 }
