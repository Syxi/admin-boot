package com.admin.module.system.service.impl;

import com.admin.module.system.mapper.SysRoleMenuMapper;
import com.admin.module.system.entity.SysRoleMenu;
import com.admin.module.system.service.SysRoleMenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色菜单关联
 */
@Service
@RequiredArgsConstructor
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取角色的菜单id集合
     *
     * @param roleId
     * @return
     */
    @Override
    public List<Long> selectMenuIds(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> roleMenuList = this.list(queryWrapper);

        // roleMenuList 过滤出 menuIds
        List<Long> meuIds = roleMenuList.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());

        return meuIds;
    }



    /**
     * 获取 menuIds
     * @param roleIds
     * @return
     */
    @Override
    public List<Long> selectMenuIds(Set<Long> roleIds) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRoleMenu::getRoleId, roleIds);
        List<SysRoleMenu> roleMenuList = this.list(queryWrapper);
        List<Long> menuIds = roleMenuList.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());
        return menuIds;
    }


    /**
     * 获取 roleIds
     *
     * @param menuIds
     * @return
     */
    @Override
    public List<SysRoleMenu> selectRoleMenuList(List<Long> menuIds) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRoleMenu::getMenuId, menuIds);
        return this.list(queryWrapper);
    }

    /**
     * 通过roleIds, 获取RoleMenu列表
     *
     * @param roleIds
     * @return
     */
    @Override
    public List<SysRoleMenu> selectRoleMenus(List<Long> roleIds) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRoleMenu::getRoleId, roleIds);
        return this.list(queryWrapper);
    }

    /**
     * 删除角色菜单的关联
     *
     * @param roleId
     */
    @Override
    public void deleteRoleMenu(Long roleId) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenu::getRoleId, roleId);
        this.remove(queryWrapper);
    }

    /**
     * 批量删除角色菜单的关联
     *
     * @param roleIds
     */
    @Override
    public void deleteBatchRoleMenu(List<Long> roleIds) {
        LambdaQueryWrapper<SysRoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRoleMenu::getRoleId, roleIds);
        this.remove(queryWrapper);
    }


}
