package com.admin.module.system.service;

import com.admin.module.system.entity.SysRole;
import com.admin.module.system.form.RoleForm;
import com.admin.module.system.query.RoleQuery;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.vo.RoleVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

public interface SysRoleService extends IService<SysRole> {

    /**
     * 角色分页列表
     * @param roleQuery
     * @return
     */
    IPage<RoleVO> selectRolePage(RoleQuery roleQuery);


    /**
     * 新增角色
     * @param roleForm
     * @return
     */
    boolean saveRole(RoleForm roleForm);


    /**
     * 更新角色
     * @param roleForm
     * @return
     */
    boolean updateRole(RoleForm roleForm);


    /**
     * 获取角色信息
     * @param roleId
     * @return
     */
    RoleVO getRoleDetail(Long roleId);



    /**
     * 删除多个角色
     * @return
     */
    boolean deleteRoles(List<Long> roleIds);



    /**
     * 启用角色
     * @param roleIds
     * @return
     */
    Boolean enableRole(List<Long> roleIds);

    /**
     * 禁用角色
     * @param roleIds
     * @return
     */
    Boolean disableRole(List<Long> roleIds);


    /**
     * 获取角色的菜单id
     * @param roleId
     * @return
     */
   List<Long> selectMenuIds(Long roleId);

    /**
     * 角色下拉选项列表
     * @return
     */
   List<OptionVO> roleTreeOption();



    /**
     * 通过 roleIds 获取role列表
     * @param roleIds
     * @return
     */
    List<SysRole> selectRoleList(List<Long> roleIds);


    /**
     * 获取role列表
     * @return
     */
    List<SysRole> selectRoleList();

    /**
     * 更新角色和用户关系
     * @param roleId
     * @param userIds
     * @return
     */
    boolean updateRoleUsers(Long roleId, List<Long> userIds);


    /**
     * 获取 roleIds
     * @param roleCodes
     * @return
     */
    Set<Long> selectRoleIds(Set<String> roleCodes);


}
