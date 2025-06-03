package com.admin.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.SysUserRole;

import java.util.List;

/**
 * @author sy
 */
public interface SysUserRoleService extends IService<SysUserRole> {


    /**
     * 新增用户角色关系
     *
     * @param userId
     * @param roleIds
     */
    void saveUserRole(Long userId, List<Long> roleIds);

    /**
     * 根据 userId 删除用户角色关系
     *
     * @param userId
     */
    void deleteUserRole(Long userId);

    /**
     * 批量删除用户角色关系
     *
     * @param userIds
     */
    void deleteBatchUserRole(List<Long> userIds);


    /**
     * 批量删除用户角色关系
     *
     * @param roleIds
     */
    void deleteBatchUserRoleList(List<Long> roleIds);


    /**
     * 通过userId， 获取roleIds
     * @param userId
     * @return
     */
    List<Long> selectRoleIds(Long userId);


    /**
     * 通过 userId 获取 userRole
     * @param userId
     * @return
     */
    List<SysUserRole> selectUserRoleList(Long userId);


    /**
     * 获取 UserId
     * @param roleId
     * @return
     */
    List<Long> selectUserIds(Long roleId);






}
