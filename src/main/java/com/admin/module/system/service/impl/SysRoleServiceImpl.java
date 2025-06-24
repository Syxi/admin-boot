package com.admin.module.system.service.impl;

import com.admin.common.constant.CacheConstants;
import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.StatusEnum;
import com.admin.common.exception.CustomException;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.entity.SysRoleMenu;
import com.admin.module.system.entity.SysUserRole;
import com.admin.module.system.form.RoleForm;
import com.admin.module.system.mapper.SysRoleMapper;
import com.admin.module.system.query.RoleQuery;
import com.admin.module.system.service.SysRoleMenuService;
import com.admin.module.system.service.SysRoleService;
import com.admin.module.system.service.SysUserRoleService;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.vo.RoleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色
 */
@Service
@Lazy
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysUserRoleService userRoleService;

    private final SysRoleMenuService roleMenuService;

    private final RedisTemplate<String, Object> redisTemplate;



    /**
     * 角色分页列表
     * @param roleQuery
     * @return
     */
    @Override
    public IPage<RoleVO> selectRolePage(RoleQuery roleQuery) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        // 非管理员登录，不显示管理员角色
        if (!SecurityUtils.isAdmin()) {
            queryWrapper.ne(SysRole::getRoleCode, SystemConstants.ADMIN_ROLE_CODE);
        }
        // 根据角色名称查询
        if (StringUtils.isNotEmpty(roleQuery.getRoleName())) {
            queryWrapper.like(SysRole::getRoleName, roleQuery.getRoleName());
        }
        // 根据角色标识查询
        if (StringUtils.isNotEmpty(roleQuery.getRoleCode())) {
            queryWrapper.like(SysRole::getRoleCode, roleQuery.getRoleCode());
        }

        // 动态设置排序规则
        if (StringUtils.isNotEmpty(roleQuery.getAscOrDesc()) && StringUtils.isNotEmpty(roleQuery.getOrderByColumn())) {
            String ascOrDesc = roleQuery.getAscOrDesc();
            if ("asc".equals(ascOrDesc)) {
                queryWrapper.orderByAsc(getSorProperty(roleQuery.getOrderByColumn()));
            } else if ("desc".equals(ascOrDesc)){
                queryWrapper.orderByDesc(getSorProperty(roleQuery.getOrderByColumn()));
            }
        } else {
            queryWrapper.orderByAsc(SysRole::getSort);
        }

        // 分页查询
        IPage<SysRole> page = new Page<>(roleQuery.getPage(), roleQuery.getLimit());
        IPage<SysRole> rolePage = this.page(page, queryWrapper);

        // IPage<Role> 转 IPage<RoleVO>
        IPage<RoleVO> roleVOPage = rolePage.convert(role -> {
            RoleVO roleVO = this.convertToRoleVO(role);
            return roleVO;
        });
        return roleVOPage;
    }

    /**
     * 获取排序字段
     * @param column
     * @return
     */
    private SFunction<SysRole, ?> getSorProperty(String column) {
        return switch (column) {
            case "sort" -> SysRole::getSort;
            case "createTime" -> SysRole::getCreateTime;
            case "updateTime" -> SysRole::getUpdateTime;
            default -> {
                log.error("排序字段不存在！" + column);
                throw new CustomException("排序字段不存在！" + column);
            }
        };
    }

    /**
     * Role 转 RoleVO
     * @param role
     * @return
     */
    private RoleVO convertToRoleVO(SysRole role) {
        RoleVO roleVO = new RoleVO();
        roleVO.setRoleId(role.getRoleId());
        roleVO.setRoleName(role.getRoleName());
        roleVO.setRoleCode(role.getRoleCode());
        roleVO.setStatus(role.getStatus());
        roleVO.setSort(role.getSort());
        roleVO.setDataScope(role.getDataScope());
        roleVO.setCreateTime(role.getCreateTime());
        roleVO.setUpdateTime(role.getUpdateTime());
        return roleVO;
    }


    /**
     * 新增更新角色
     * @param roleForm
     * @return
     */
    @Override
    public boolean saveRole(RoleForm roleForm) {
        // 检验角色是否已存在
        boolean count =  this.isExistsRole(roleForm);
        if (count) {
            throw new CustomException("角色名或角色编码重复，请检查！");
        }

        // RoleForm 对象转换 Role
        SysRole role = this.convertToRole(roleForm);
        boolean result =  this.save(role);
        return result;
    }


    /**
     * 检验角色是否已存在
     * @param roleForm
     * @return
     */
    boolean isExistsRole(RoleForm roleForm) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        if (roleForm.getRoleId() != null) {
            queryWrapper.ne(SysRole::getRoleId, roleForm.getRoleId());
        }
        queryWrapper.and(wrapper -> wrapper
                        .eq(SysRole::getRoleName, roleForm.getRoleName())
                        .or()
                        .eq(SysRole::getRoleCode, roleForm.getRoleCode()));
        queryWrapper.eq(SysRole::getDeleted, DeletedEnum.NO_DELETE.getValue());
        boolean count =  this.exists(queryWrapper);
        return count;
    }

    /**
     * 更新角色
     *
     * @param roleForm
     * @return
     */
    @Override
    public boolean updateRole(RoleForm roleForm) {
        // 检验角色是否已存在
        boolean count =  this.isExistsRole(roleForm);
        if (count) {
            throw new CustomException("角色名或角色编码重复，请检查！");
        }

        // 获取 roleCode
        SysRole sysRole = this.getById(roleForm.getRoleId());
        String oldRoleCode = sysRole.getRoleCode();
        String newRoleCode = roleForm.getRoleCode();

        // 更新角色
        SysRole role = this.convertToRole(roleForm);
        boolean result =  this.updateById(role);

        // 跟新角色权限缓存
        if (result && !oldRoleCode.equals(newRoleCode)) {
            Set<String> perms = (Set<String>) redisTemplate.opsForHash().get(CacheConstants.ROLE_PERMS_PREFIX, oldRoleCode);
            redisTemplate.opsForHash().delete(CacheConstants.ROLE_PERMS_PREFIX, oldRoleCode);
            redisTemplate.opsForHash().put(CacheConstants.ROLE_PERMS_PREFIX, newRoleCode, perms);
        }
        return result;
    }


    private SysRole convertToRole(RoleForm roleForm) {
        SysRole role = new SysRole();
        role.setRoleId(roleForm.getRoleId());
        role.setRoleName(roleForm.getRoleName());
        role.setRoleCode(roleForm.getRoleCode());
        role.setSort(roleForm.getSort());
        role.setDataScope(roleForm.getDataScope());
        role.setStatus(roleForm.getStatus());
        role.setRemark(roleForm.getRemark());
        role.setCreateTime(roleForm.getCreateTime());
        role.setUpdateTime(roleForm.getUpdateTime());
        return role;
    }


    /**
     * 获取角色信息
     *
     * @param roleId
     * @return
     */
    @Override
    public RoleVO getRoleDetail(Long roleId) {
        SysRole role = this.getById(roleId);
        RoleVO roleVO = this.convertToRoleVO(role);
        return roleVO;
    }



    /**
     * 删除多个角色
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteRoles(List<Long> roleIds) {

        roleIds.forEach(roleId -> {
            long count = userRoleService.count(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
            if ( count > 0L) {
                throw new CustomException("该角色已分配用户，无法删除！");
            }
        });

        // 删除角色权限缓存
        List<String> roleCodes = this.list(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roleIds)).stream()
                .map(SysRole::getRoleCode).collect(Collectors.toList());
        roleCodes.forEach(roleCode -> {
            redisTemplate.opsForHash().delete(CacheConstants.ROLE_PERMS_PREFIX, roleCode);
        });


        // 删除所有角色和菜单关联
        roleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        // 批量删除角色
        boolean result = this.removeByIds(roleIds);
        return result;
    }







    /**
     * 启用角色
     *
     * @param roleIds
     * @return
     */
    @Override
    public Boolean enableRole(List<Long> roleIds) {
        LambdaUpdateWrapper<SysRole> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysRole::getStatus, StatusEnum.ENABLE.getValue());
        updateWrapper.in(SysRole::getRoleId, roleIds);
        return this.update(updateWrapper);

    }

    /**
     * 禁用角色
     *
     * @param roleIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean disableRole(List<Long> roleIds) {
        LambdaUpdateWrapper<SysRole> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysRole::getStatus, StatusEnum.DISABLE.getValue());
        updateWrapper.in(SysRole::getRoleId, roleIds);
        boolean result = this.update(updateWrapper);

//        if (result) {
//            // 删除角色用户关联
//            userRoleService.deleteBatchUserRoleList(roleIds);
//            // 删除角色菜单关联
//            roleMenuService.deleteBatchRoleMenu(roleIds);
//            // 删除角色权限缓存
//            roleMenuService.refreshRolePermsCache();
//        }

        return result;
    }




    /**
     * 获取角色的菜单id
     *
     * @param roleId
     * @return
     */
//    @Cacheable(cacheNames = "getRoleMenuIds", key = "#roleId")
    @Override
    public List<Long> selectMenuIds(Long roleId) {
        List<Long> menuIds = roleMenuService.selectMenuIds(roleId);
        return menuIds;
    }

    /**
     * 角色下拉选项列表
     *
     * @return
     */
    @Override
    public List<OptionVO> roleTreeOption() {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        // 非管理员，不显示管理员角色选项
        if (!SecurityUtils.isAdmin()) {
            queryWrapper.ne(SysRole::getRoleCode, SystemConstants.ADMIN_ROLE_CODE);
        }
        queryWrapper.select(SysRole::getRoleId, SysRole::getRoleName);
        queryWrapper.orderByAsc(SysRole::getSort);
        List<SysRole> roleList = this.list(queryWrapper);

        // Role 转换 Option
        if (CollectionUtils.isNotEmpty(roleList)) {
            List<OptionVO> optionVOList = roleList.stream()
                    .map(role -> this.convertToRoleOption(role))
                    .collect(Collectors.toList());
            return optionVOList;
        }

        return Collections.EMPTY_LIST;
    }


    /**
     * SysRole 转 Option
     * @param role
     * @return
     */
    private OptionVO convertToRoleOption(SysRole role) {
        OptionVO optionVo = new OptionVO();
        optionVo.setValue(role.getRoleId());
        optionVo.setLabel(role.getRoleName());
        return optionVo;
    }




    /**
     * 通过 roleIds 获取role列表
     *
     * @param roleIds
     * @return
     */
    @Override
    public List<SysRole> selectRoleList(List<Long> roleIds) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRole::getRoleId, roleIds);
        List<SysRole> roleList = this.list(queryWrapper);
        return roleList;
    }

    /**
     * 获取role列表
     *
     * @return
     */
    @Override
    public List<SysRole> selectRoleList() {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getStatus, StatusEnum.ENABLE.getValue());
        queryWrapper.eq(SysRole::getDeleted, DeletedEnum.NO_DELETE.getValue());
        List<SysRole> roleList = this.list(queryWrapper);
        return roleList;
    }


    /**
     * 更新角色和用户关系
     * @param roleId
     * @param userIds
     * @return
     */
    @Override
    public boolean updateRoleUsers(Long roleId, List<Long> userIds) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, roleId);
        List<SysUserRole> sysUserRoles = userRoleService.list(wrapper);
        userRoleService.removeBatchByIds(sysUserRoles);

        List<SysUserRole> sysUserRoleList = userIds.stream()
                .map(userId -> new SysUserRole(userId, roleId))
                .collect(Collectors.toList());

        userRoleService.saveBatch(sysUserRoleList);
        return true;
    }

    /**
     * 获取 roleIds
     *
     * @param roleCodes
     * @return
     */
    @Override
    public Set<Long> selectRoleIds(Set<String> roleCodes) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRole::getRoleCode, roleCodes);
        Set<Long> roleIds = this.list(queryWrapper).stream()
                .map(SysRole::getRoleId)
                .collect(Collectors.toSet());
        return roleIds;
    }


}
