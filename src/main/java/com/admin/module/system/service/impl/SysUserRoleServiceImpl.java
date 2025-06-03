package com.admin.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.module.system.mapper.SysUserRoleMapper;
import com.admin.module.system.entity.SysUserRole;
import com.admin.module.system.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 用户角色关联
 */
@RequiredArgsConstructor
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {



    /**
     * 新增用户角色关系
     *
     * @param userId
     * @param roleIds
     */
    @Override
    public void saveUserRole(Long userId, List<Long> roleIds) {
        List<SysUserRole> userRoleList = roleIds
                .stream()
                .map(roleId -> new SysUserRole(userId, roleId))
                .collect(Collectors.toList());
       this.saveBatch(userRoleList);
    }

    /**
     * 删除userId 的用户角色关系
     *
     * @param userId
     */
    @Override
    public void deleteUserRole(Long userId) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        this.remove(queryWrapper);
    }

    /**
     * 批量删除用户角色关系
     *
     * @param userIds
     */
    @Override
    public void deleteBatchUserRole(List<Long> userIds) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUserRole::getUserId, userIds);
        this.remove(queryWrapper);
    }

    /**
     * 批量删除用户角色关系
     *
     * @param roleIds
     */
    @Override
    public void deleteBatchUserRoleList(List<Long> roleIds) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUserRole::getRoleId, roleIds);
        this.remove(queryWrapper);
    }

    /**
     * 通过userId， 获取roleIds
     *
     * @param userId
     * @return
     */
    @Override
    public List<Long> selectRoleIds(Long userId) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        queryWrapper.select(SysUserRole::getRoleId);
        List<Long> roleIdList = this.list(queryWrapper)
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
        return roleIdList;
    }

    /**
     * 通过 userId 获取 userRole
     *
     * @param userId
     * @return
     */
    @Override
    public List<SysUserRole> selectUserRoleList(Long userId) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoleList = this.list(queryWrapper);
        return userRoleList;
    }

    /**
     * 获取UserId
     *
     * @param roleId
     * @return
     */
    @Override
    public List<Long> selectUserIds(Long roleId) {
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getRoleId, roleId);
        List<SysUserRole> userRoleList = this.list(queryWrapper);

        List<Long> userList = userRoleList.stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .collect(Collectors.toList());

        return userList;
    }





}
