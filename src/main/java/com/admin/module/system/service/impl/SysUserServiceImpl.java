package com.admin.module.system.service.impl;

import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.GenderEnum;
import com.admin.common.enums.IBaseEnum;
import com.admin.common.enums.StatusEnum;
import com.admin.common.exception.CustomException;
import com.admin.common.result.ResultVO;
import com.admin.common.security.PermissionService;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.dto.PasswordUpdateDTO;
import com.admin.module.system.dto.UserAuthInfo;
import com.admin.module.system.dto.UserInfoUpdateDTO;
import com.admin.module.system.entity.*;
import com.admin.module.system.form.UserForm;
import com.admin.module.system.mapper.SysUserMapper;
import com.admin.module.system.query.UserQuery;
import com.admin.module.system.service.*;
import com.admin.module.system.vo.TransferVO;
import com.admin.common.excel.export.UserExportVO;
import com.admin.module.system.vo.UserInfoVO;
import com.admin.module.system.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 用户
 * @author suyan
 */

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleService userRoleService;

    private final SysMenuService menuService;

    private final SysRoleService roleService;

    private final SysRoleMenuService roleMenuService;

    private final SysDeptService sysDeptService;

    private final PasswordEncoder passwordEncoder;

    private final PermissionService permissionService;

    private final UserLoginLogService  userLoginLogService;



    /**
     * 获取用户分页列表
     * @param userQuery
     * @return
     */
    @Override
    public IPage<UserVO> selectUserPage(UserQuery userQuery) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        // 不是管理员，不显示 admin
        if (!SecurityUtils.isAdmin()) {
            queryWrapper.ne(SysUser::getUsername, SystemConstants.ADMIN_ROLE_NAME);
        }
        // 用户名
        if (StringUtils.isNotEmpty(userQuery.getUsername())) {
            queryWrapper.like(SysUser::getUsername, userQuery.getUsername())
                    .or().like(SysUser::getRealName, userQuery.getUsername());
        }
//        if (StringUtils.isNotEmpty(userQuery.getRealName())) {
//            queryWrapper.like(SysUser::getRealName, userQuery.getRealName());
//        }
        // 组织id
        if (userQuery.getOrganId() != null) {
            queryWrapper.eq(SysUser::getOrganId, userQuery.getOrganId());
        }
        // 用户状态
        if (userQuery.getStatus() != null) {
            queryWrapper.eq(SysUser::getStatus, userQuery.getStatus());
        }
        // 创建时间
        if (userQuery.getStartTime() != null && userQuery.getEndTime() != null) {
            queryWrapper.between(SysUser::getCreateTime, userQuery.getStartTime(), userQuery.getEndTime());
        }
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> page = new Page<>(userQuery.getPage(), userQuery.getLimit());
        IPage<SysUser> pageData = this.page(page, queryWrapper);

        // userId、roleNames存储在map中，key是userId, value是roleNames
        Map<Long, List<String>> userIdRoleNamesMap = this.buildUserIdRoleNamesMap();

        // dept 的id、name的关系存储在map中，key是id, name是value
        Map<Long, String> deptIdNameMap = this.buildDeptIdRoleNamesMap();

        IPage<UserVO> userVOIPage = pageData.convert(user -> {
            // user 转 userVO
            UserVO userVO = this.convertToUserVO(user);

            List<String> roleNameList = userIdRoleNamesMap.getOrDefault(user.getUserId(), Collections.emptyList());
            String roleNames = String.join(",", roleNameList);
            userVO.setRoleNames(roleNames);

            if (user.getOrganId() != null) {
                String organName = deptIdNameMap.get(user.getOrganId());
                userVO.setOrganName(organName);
            }

            return userVO;
        });


        return userVOIPage;
    }


    /**
     * userId、roleNames存储在map中，key是userId, value是roleNames
     * @return
     */
    private Map<Long, List<String>> buildUserIdRoleNamesMap() {
        // 用户角色关联表
        List<SysUserRole> userRoleList = userRoleService.list();
        if (CollectionUtils.isEmpty(userRoleList)) {
            return Collections.emptyMap();
        }
        List<Long> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(toList());

        // roleId roleName映射
        List<SysRole> roleList = roleService.selectRoleList(roleIds);
        Map<Long, String> roleIdRoleNameMap = roleList.stream()
                .collect(Collectors.toMap(SysRole::getRoleId, SysRole::getRoleName));

        // userId 和 roleNames映射
        Map<Long, List<String>> userIdRoleNamesMap = userRoleList.stream()
                .collect(Collectors.groupingBy(
                        SysUserRole::getUserId,
                        Collectors.mapping(
                                userRole -> {
                                    return Optional.ofNullable(roleIdRoleNameMap.get(userRole.getRoleId()))
                                            .orElse("用户还没有分配角色");
                                },
                                Collectors.toList()
                        )
                ));

        return userIdRoleNamesMap;
    }


    /**
     * dept 的id、name的关系存储在map中，key是id, value是name
     * @return
     */
    private Map<Long, String> buildDeptIdRoleNamesMap() {
        List<SysDept> deptList = sysDeptService.list();
        Map<Long, String> idNameMap = deptList.stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
        return idNameMap;
    }



    /**
     * User 转 UserVO
     * @param user
     * @return
     */
    private UserVO convertToUserVO(SysUser user) {
        UserVO userVO = new UserVO();
        userVO.setUserId(user.getUserId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName());
        userVO.setMobile(user.getMobile());
        userVO.setSort(user.getSort());
        userVO.setRemark(user.getRemark());
        userVO.setEmail(user.getEmail());
        userVO.setStatus(user.getStatus());
        userVO.setCreateTime(user.getCreateTime());

        if (user.getGender() != null) {
            String genderLabel = IBaseEnum.getLabelByValue(user.getGender(), GenderEnum.class);
            userVO.setGenderLabel(genderLabel);
        }

        return userVO;
    }


    /**
     * 新增用户
     * @param userForm
     * @return
     */
    @Override
    public boolean saveUser(UserForm userForm) {
        // 判断用户名是否存在
        if (existsUsername(userForm.getUsername(), null)) {
            throw new CustomException("用户名已存在");
        }

        // UserForm 对象转换 User
        SysUser user = new SysUser();
        user.setUserId(userForm.getUserId());
        user.setUsername(userForm.getUsername());
        user.setRealName(userForm.getRealName());
        user.setGender(userForm.getGender());
        user.setAvatar(userForm.getAvatar());
        user.setMobile(userForm.getMobile());
        user.setStatus(userForm.getStatus());
        user.setEmail(userForm.getEmail());
        user.setSort(userForm.getSort());
        user.setRemark(userForm.getRemark());
        user.setOrganId(userForm.getOrganId());
        String password = passwordEncoder.encode(SystemConstants.DEFAULT_PASSWORD);
        user.setPassword(password);

        // 新增用户
        boolean result = this.save(user);
        return result;
    }


    /**
     * 校验数据库是否存在用户名
     * @param username
     * @param userId
     * @return
     */
    private boolean existsUsername(String username, Long userId) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());

        if (userId != null) {
            queryWrapper.ne(SysUser::getUserId, userId);
        }
        boolean result = baseMapper.exists(queryWrapper);
        return result;
    }


    /**
     * 更新用户
     * @param userForm
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateUser(UserForm userForm) {
        // 判断用户名是否存在
        if (existsUsername(userForm.getUsername(), userForm.getUserId())) {
            throw new CustomException("用户名已存在");
        }

        // UserForm 对象转换 User
        SysUser user = new SysUser();
        user.setUserId(userForm.getUserId());
        user.setUsername(userForm.getUsername());
        user.setRealName(userForm.getRealName());
        user.setGender(userForm.getGender());
        user.setAvatar(userForm.getAvatar());
        user.setMobile(userForm.getMobile());
        user.setStatus(userForm.getStatus());
        user.setEmail(userForm.getEmail());
        user.setSort(userForm.getSort());
        user.setRemark(userForm.getRemark());
        user.setOrganId(userForm.getOrganId());
        // 更新用户
        boolean result = this.updateById(user);
        return result;
    }

    /**
     * 获取用户详情
     *
     * @param userId
     * @return
     */
    @Override
    public UserForm getUserDetail(Long userId) {
        SysUser user = this.getById(userId);

        // User 对象转换 UserForm
        UserForm userForm = new UserForm();
        userForm.setUserId(user.getUserId());
        userForm.setUsername(user.getUsername());
        userForm.setPassword(user.getPassword());
        userForm.setRealName(user.getRealName());
        userForm.setMobile(user.getMobile());
        userForm.setGender(user.getGender());
        userForm.setAvatar(user.getAvatar());
        userForm.setEmail(user.getEmail());
        userForm.setStatus(user.getStatus());
        userForm.setSort(user.getSort());
        userForm.setRemark(user.getRemark());
        userForm.setOrganId(user.getOrganId());

        // 获取角色id
        List<Long> roleIds = userRoleService.selectRoleIds(userId);
        userForm.setRoleIds(roleIds);
        return userForm;
    }





    /**
     * 批量删除
     * @param userIds
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatchUsers(List<Long> userIds) {
        boolean result = this.removeBatchByIds(userIds);

        // 删除用户角色关系
        if (result) {
            userRoleService.deleteBatchUserRole(userIds);
        }
        return result;
    }

    /**
     * 启用用户
     *
     * @param userIds
     * @return
     */
    @Override
    public boolean enableUser(List<Long> userIds) {
        LambdaUpdateWrapper<SysUser> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.in(SysUser::getUserId, userIds);
        queryWrapper.set(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        boolean result = this.update(queryWrapper);
        return result;
    }

    /**
     * 禁用用户
     *
     * @param userIds
     * @return
     */
    @Override
    public boolean disableUser(List<Long> userIds) {
        LambdaUpdateWrapper<SysUser> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.in(SysUser::getUserId, userIds);
        queryWrapper.set(SysUser::getStatus, StatusEnum.DISABLE.getValue());
        boolean result = this.update(queryWrapper);

        // 禁用用户 不删除用户角色关联
//        if (result) {
//            userRoleService.deleteBatchUserRole(userIds);
//        }

        return result;
    }


    /**
     * 重置密码
     *
     * @param userIds 用户id
     * @return
     */
    @Override
    public String resetPassword(List<Long> userIds)  {
        String newPassword = passwordEncoder.encode(SystemConstants.DEFAULT_PASSWORD);
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(SysUser::getUserId, userIds);
        updateWrapper.set(SysUser::getPassword, newPassword);
        boolean result = this.update(updateWrapper);
        if (result) {
            return SystemConstants.DEFAULT_PASSWORD;
        }

        return null;
    }


    /**
     *  根据username,获取登录认证信息
     * @param username 用户名
     * @return User
     */
    @Override
    public UserAuthInfo getUserAuthInfo(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        queryWrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        queryWrapper.last("limit 1");
        SysUser user = this.getOne(queryWrapper);

        // User 转 UserAuthInfo
        UserAuthInfo userAuthInfo = this.convertToUserAuthInfo(user);

        // 用户角色关联表
        List<SysUserRole> userRoleList = userRoleService.selectUserRoleList(user.getUserId());
        if (CollectionUtils.isNotEmpty(userRoleList)) {
            Set<Long> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
            // 角色列表
            List<SysRole> roleList = roleService.listByIds(roleIds);

            // 角色编码
            Set<String> roleCodes = roleList.stream()
                    .map(SysRole::getRoleCode)
                    .collect(Collectors.toSet());
            userAuthInfo.setRoles(roleCodes);

            // 数据权限
            Integer dataScope = roleList.stream()
                    .map(SysRole::getDataScope)
                    .min(Integer::compare)
                    .orElse(null);
            if (dataScope != null) {
                userAuthInfo.setDataScope(dataScope);
            }


            // 根据 roleId, 获取 menuIds
            List<Long> menuIds = roleMenuService.selectMenuIds(roleIds);
            if (CollectionUtils.isNotEmpty(menuIds)) {
                // 菜单权限列表
                Set<String> permissions = menuService.selectMenuPerms(menuIds);
                userAuthInfo.setPermissions(permissions);
            }

        }









        return userAuthInfo;
    }



    /**
     * 获取当前登录用户信息
     *
     * @param username
     * @return
     */
    @Override
    public UserInfoVO getCurrentUserInfo(String username, Set<String> roleCodes) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username).last("limit 1");
        SysUser user = this.getOne(queryWrapper);

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(user.getUserId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setGender(user.getGender());
        userInfoVO.setMobile(user.getMobile());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setAvatar(user.getAvatar());
        // 用户角色编码
        userInfoVO.setRoles(roleCodes);


        if (CollectionUtils.isNotEmpty(roleCodes)) {
            // 角色集合
            LambdaQueryWrapper<SysRole> roleQueryWrapper = new LambdaQueryWrapper<>();
            roleQueryWrapper.in(SysRole::getRoleCode, roleCodes);
            List<SysRole> roleList = roleService.list(roleQueryWrapper);

            Set<Long> roleIds = roleList.stream()
                    .map(SysRole::getRoleId)
                    .collect(Collectors.toSet());

            List<Long> menuIds = roleMenuService.selectMenuIds(roleIds);

            // 用户权限标识
            Set<String> permissions = menuService.selectMenuPerms(menuIds);
            userInfoVO.setPerms(permissions);

            // 角色名称
            String roleNames = roleList.stream()
                            .map(SysRole::getRoleName)
                            .collect(Collectors.joining(","));
            userInfoVO.setRoleNames(roleNames);
        }

        // 用户部门名称
//        if (user.getOrganId() != null) {
//            String deptName = sysDeptService.getById(user.getOrganId()).getDeptName();
//            userInfoVO.setDeptNames(Set.of(deptName));
//        }

        // 用户最后登录时间
        LambdaQueryWrapper<UserLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLoginLog::getUserId, user.getUserId());
        wrapper.orderByDesc(UserLoginLog::getLoginTime);
        wrapper.last("limit 1");
        UserLoginLog userLoginLog = userLoginLogService.getOne(wrapper);
        LocalDateTime lastLoginTime = userLoginLog.getLoginTime();
        userInfoVO.setLastLoginTime(lastLoginTime);




        return userInfoVO;
    }





    /**
     * User 转 UserAuthInfo
     * @param user
     * @return
     */
    private UserAuthInfo convertToUserAuthInfo(SysUser user) {
        UserAuthInfo userAuthInfo = new UserAuthInfo();
        userAuthInfo.setUserId(user.getUserId());
        userAuthInfo.setUsername(user.getUsername());
        userAuthInfo.setPassword(user.getPassword());
        userAuthInfo.setRealName(user.getRealName());
        userAuthInfo.setStatus(user.getStatus());
        userAuthInfo.setOrganId(user.getOrganId());
        return userAuthInfo;
    }



    /**
     * 导出用户列表
     *
     * @param
     * @return
     */
    @Override
    public List<UserExportVO> exportUsers(List<Long> userIds) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SysUser::getUsername, "admin");
        wrapper.in(SysUser::getUserId, userIds);
        wrapper.groupBy(SysUser::getUserId);
        List<SysUser> userList = this.list(wrapper);

        // userId 和 roleNames 映射
        Map<Long, List<String>> userIdRoleNamesMap = buildUserIdToRoleNamesMapping();

        List<UserExportVO> userExportVOS = userList.stream()
                .map(this::convertToUserExportVO)
                .peek(userExportVO -> {
                    // 角色名称
                    List<String> roleNames = userIdRoleNamesMap.getOrDefault(userExportVO.getUserId(), Collections.emptyList());
                    userExportVO.setRoleNames(String.join(",", roleNames));
                })
                .collect(toList());


        return userExportVOS;
    }


    /**
     * 构建userId 和 roleNames 映射
     * @return
     */
    private Map<Long, List<String>> buildUserIdToRoleNamesMapping() {
        // 用户角色关联表
        List<SysUserRole> userRoleList = userRoleService.list();
        List<Long> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(toList());

        List<SysRole> roleList = roleService.selectRoleList(roleIds);
        // roleId 和 roleName映射
        Map<Long, String> roleIdRoleNameMap = roleList.stream()
                .collect(Collectors.toMap(SysRole::getRoleId, SysRole::getRoleName));

        // userId 和 roleNames 映射
        Map<Long, List<String>> userIdRoleNamesMap = userRoleList.stream()
                .collect(Collectors.groupingBy(
                        SysUserRole::getUserId,
                        Collectors.mapping(userRole -> roleIdRoleNameMap.get(userRole.getRoleId()), toList())
                ));
        return userIdRoleNamesMap;
    }


    /**
     * User 转 UserExportVO
     * @param user
     * @return
     */
    private UserExportVO convertToUserExportVO(SysUser user) {
        UserExportVO userExportVo = new UserExportVO();
        userExportVo.setUserId(user.getUserId());
        userExportVo.setUsername(user.getUsername());
        userExportVo.setRealName(user.getRealName());

        if (user.getGender() != null) {
            String genderLabel = IBaseEnum.getLabelByValue(user.getGender(), GenderEnum.class);
            userExportVo.setGender(genderLabel);
        }

        userExportVo.setMobile(user.getMobile());
        userExportVo.setEmail(user.getEmail());
        userExportVo.setCreateTime(user.getCreateTime());
        return userExportVo;
    }




    /**
     * 获取角色的用户列表
     *
     * @param roleId
     * @return
     */
    @Override
    public List<TransferVO> selectUserListInRole(Long roleId) {
        List<Long> userIds = userRoleService.selectUserIds(roleId);
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUser::getUserId, userIds);
        wrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        wrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        wrapper.orderByDesc(SysUser::getCreateTime);
        List<SysUser> userList = this.list(wrapper);
        List<TransferVO> transferVOList = userList.stream()
                .map(this::convertToTransferVO)
                .collect(toList());
        return transferVOList;
    }


    /**
     * 实体转换
     * @param sysUser
     * @return
     */
    private TransferVO convertToTransferVO(SysUser sysUser) {
        TransferVO transferVO = new TransferVO();
        transferVO.setKey(sysUser.getUserId());
        transferVO.setLabel(sysUser.getUsername());
        return  transferVO;
    }


    /**
     * 获取不属于该角色的用户列表
     *
     * @param roleId
     * @return
     */
    @Override
    public List<TransferVO> selectUserListNotInRole(Long roleId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        wrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        wrapper.orderByDesc(SysUser::getCreateTime);
        return this.list(wrapper).stream()
                .map(this::convertToTransferVO)
                .collect(toList());

    }

    /**
     * 更新密码
     *
     * @param userId
     * @return
     */
    @Override
    public ResultVO<Boolean> updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO) {
        SysUser user = this.getById(userId);

        if (user == null) {
            return ResultVO.error("用户不存在");
        }

        String newPassword = passwordUpdateDTO.getNewPassword();
        String oldPassword = passwordUpdateDTO.getOldPassword();

        boolean matchesNewPassword = newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$");
        if (!matchesNewPassword) {
            return ResultVO.error("新密码必须由字符和数字组成");
        }

        if (passwordEncoder.matches(oldPassword, user.getPassword()) && StringUtils.isNotBlank(newPassword)) {
            String encodeNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodeNewPassword);
            boolean result = this.updateById(user);
            return ResultVO.success(result);
        } else {
            return ResultVO.error("原密码错误");
        }


    }

    /**
     * 个人信息
     *
     * @param userId
     * @return
     */
    @Override
    public UserInfoUpdateDTO getUserInfoUpdateDTO(Long userId) {
        SysUser sysUser = this.getById(userId);

        UserInfoUpdateDTO userInfo = new UserInfoUpdateDTO();
        userInfo.setUserId(sysUser.getUserId());
        userInfo.setUsername(sysUser.getUsername());
        userInfo.setRealName(sysUser.getRealName());
        userInfo.setMobile(sysUser.getMobile());
        userInfo.setEmail(sysUser.getEmail());
        userInfo.setStatus(sysUser.getStatus());
        userInfo.setSort(sysUser.getSort());
        userInfo.setRemark(sysUser.getRemark());
        userInfo.setCreateTime(sysUser.getCreateTime());

        if (sysUser.getGender() != null) {
            String genderLabel = IBaseEnum.getLabelByValue(sysUser.getGender(), GenderEnum.class);
            userInfo.setGenderLabel(genderLabel);
        }


        // 角色名称
        Map<Long, List<String>> userIdRoleNamesMap = this.buildUserIdRoleNamesMap();
        List<String> roleNameList = userIdRoleNamesMap.getOrDefault(userId, Collections.emptyList());
        String roleNames = String.join(",", roleNameList);
        userInfo.setRoleNames(roleNames);

        // 组织名称
        Map<Long, String> organIdOrganNameMap = this.buildDeptIdRoleNamesMap();
        String organName = organIdOrganNameMap.get(sysUser.getOrganId());
        userInfo.setOrganName(organName);

        return userInfo;
    }

    /**
     * 更新个人信息
     *
     * @param userInfo
     * @return
     */
    @Override
    public boolean updateUserInfo(Long userId, UserInfoUpdateDTO userInfo) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setRealName(userInfo.getRealName());
        sysUser.setGender(userInfo.getGender());
        sysUser.setMobile(userInfo.getMobile());
        sysUser.setEmail(userInfo.getEmail());
        return this.updateById(sysUser);
    }


}
