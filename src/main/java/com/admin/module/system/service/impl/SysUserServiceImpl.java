package com.admin.module.system.service.impl;

import com.admin.common.annotation.DataPermission;
import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.*;
import com.admin.common.excel.export.UserExportVO;
import com.admin.common.exception.CustomException;
import com.admin.common.result.ResultVO;
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

    private final UserDataService userDataService;

    private final SysRoleMenuService roleMenuService;

    private final SysDeptService sysDeptService;

    private final PasswordEncoder passwordEncoder;

    private final UserLoginLogService  userLoginLogService;


    /**
     * 根据用户ID列表获取用户名
     *
     * @param userIds 用户ID列表
     * @return 用户名列表
     */
    @Override
    public List<String> getUsernamesByIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUser::getUserId, userIds);
        queryWrapper.select(SysUser::getUsername); // 只查询username字段

        List<SysUser> userList = this.list(queryWrapper);
        return userList.stream()
                .map(SysUser::getUsername)
                .collect(Collectors.toList());
    }


    /**
     * 获取用户分页列表（优化版：按需查询，避免全表加载）
     * @param userQuery
     * @return
     */
    @DataPermission()
    @Override
    public IPage<UserVO> selectUserPage(UserQuery userQuery) {
        // 1. 先构建查询条件，查询用户主表
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        // 不是管理员，不显示 admin
        if (!SecurityUtils.isAdmin()) {
            queryWrapper.ne(SysUser::getUsername, SystemConstants.ADMIN_ROLE_NAME);
        }
        // 用户名或真实姓名
        if (StringUtils.isNotBlank(userQuery.getKeywords())) {
            queryWrapper.like(SysUser::getUsername, userQuery.getKeywords())
                    .or().like(SysUser::getRealName, userQuery.getKeywords());
        }
        
        // 2. 如果按部门筛选，只查询该部门下的用户ID（按需查询）
        if (userQuery.getDeptId() != null) {
            queryWrapper.eq(SysUser::getDeptId, userQuery.getDeptId());
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

        
        // 3. 分页查询用户主表
        IPage<SysUser> page = new Page<>(userQuery.getPage(), userQuery.getLimit());
        IPage<SysUser> pageData = this.page(page, queryWrapper);
        
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new Page<>(userQuery.getPage(), userQuery.getLimit());
        }
        
        // 4. 只查询当前页用户的关联数据（按需查询，不是全表）
        List<Long> currentPageUserIds = pageData.getRecords().stream()
            .map(SysUser::getUserId)
            .collect(Collectors.toList());
        
        // 只查询当前页用户的部门信息
        Set<Long> deptIds = pageData.getRecords().stream()
            .map(SysUser::getDeptId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, String> deptIdNameMap = buildDeptIdNameMapByDeptIds(new ArrayList<>(deptIds));
        
        // 只查询当前页用户的角色信息
        Map<Long, List<String>> userIdRoleNamesMap = buildUserIdRoleNamesMapByUserIds(currentPageUserIds);
        
        // 5. 组装VO
        IPage<UserVO> userVOIPage = pageData.convert(user -> {
            UserVO userVO = this.convertToUserVO(user);
            
            List<String> roleNameList = userIdRoleNamesMap.getOrDefault(user.getUserId(), Collections.emptyList());
            String roleNames = String.join(",", roleNameList);
            userVO.setRoleNames(roleNames);
            
            Long deptId = user.getDeptId();
            String deptName = deptIdNameMap.get(deptId);
            userVO.setDeptName(deptName);
            
            return userVO;
        });
        
        return userVOIPage;
    }


    /**
     * 按部门ID列表查询部门名称（按需查询 + 缓存）
     * @param deptIds
     * @return
     */
    private Map<Long, String> buildDeptIdNameMapByDeptIds(List<Long> deptIds) {
        if (CollectionUtils.isEmpty(deptIds)) {
            return Collections.emptyMap();
        }
        List<SysDept> deptList = sysDeptService.list(
            new LambdaQueryWrapper<SysDept>()
                .in(SysDept::getId, deptIds)
        );
        return deptList.stream()
            .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName, (v1, v2) -> v1));
    }

    /**
     * 按用户ID列表查询角色名称（按需查询）
     * @param userIds
     * @return
     */
    private Map<Long, List<String>> buildUserIdRoleNamesMapByUserIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        
        // 只查询当前用户的角色关系
        List<SysUserRole> userRoleList = userRoleService.list(
            new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds)
        );
        
        if (CollectionUtils.isEmpty(userRoleList)) {
            return Collections.emptyMap();
        }
        
        List<Long> roleIds = userRoleList.stream()
            .map(SysUserRole::getRoleId)
            .distinct()
            .collect(toList());
        
        // 只查询涉及的角色
        List<SysRole> roleList = userDataService.selectRoleList(roleIds);
        Map<Long, String> roleIdRoleNameMap = roleList.stream()
            .collect(Collectors.toMap(SysRole::getRoleId, SysRole::getRoleName, (v1, v2) -> v1));
        
        return userRoleList.stream()
            .collect(Collectors.groupingBy(
                SysUserRole::getUserId,
                Collectors.mapping(
                    userRole -> Optional.ofNullable(roleIdRoleNameMap.get(userRole.getRoleId()))
                        .orElse("用户还没有分配角色"),
                    Collectors.toList()
                )
            ));
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
        user.setDeptId(userForm.getDeptId()); // 设置部门ID
        String password = passwordEncoder.encode(SystemConstants.DEFAULT_PASSWORD);
        user.setPassword(password);

        // 新增用户
        return this.save(user);
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
        user.setDeptId(userForm.getDeptId()); // 设置部门ID

        // 更新用户
        return this.updateById(user);
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
        userForm.setDeptId(user.getDeptId()); // 设置部门ID

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
     *  根据username,获取登录认证信息， 存储在 SecurityContext 中
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

        // 部门
        userAuthInfo.setDeptId(user.getDeptId());

        // 用户角色关联表
        List<SysUserRole> userRoleList = userRoleService.selectUserRoleList(user.getUserId());
        if (CollectionUtils.isNotEmpty(userRoleList)) {
            Set<Long> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
            // 角色列表
            List<SysRole> roleList = userDataService.getRolesByIds(new ArrayList<>(roleIds));

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


            // 菜单权限
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
     * 前端获取当前登录用户信息
     *
     * @param username
     * @return
     */
    @Override
    public UserInfoVO getCurrentUserInfo(String username) {
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
        List<Long> roleIds = userRoleService.selectRoleIds(user.getUserId());
        List<SysRole> roleList = userDataService.selectRoleList(roleIds);
        Set<String> roleCodes = roleList.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toSet());
        userInfoVO.setRoles(roleCodes);

        // 角色名称
        String roleNames = roleList.stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.joining(","));
        userInfoVO.setRoleNames(roleNames);


        if (CollectionUtils.isNotEmpty(roleCodes)) {
            Set<Long> roleIdsSet = new HashSet<>(roleIds);
            List<Long> menuIds = roleMenuService.selectMenuIds(roleIdsSet );

            // 用户权限标识
            Set<String> permissions = menuService.selectMenuPerms(menuIds);
            userInfoVO.setPerms(permissions);


        }

        // 用户部门名称
        // 用户最后登录时间
        LambdaQueryWrapper<UserLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLoginLog::getUserId, user.getUserId());
        wrapper.orderByDesc(UserLoginLog::getLoginTime);
        wrapper.last("limit 1");
        UserLoginLog userLoginLog = userLoginLogService.getOne(wrapper);
        if (userLoginLog != null) {
            LocalDateTime lastLoginTime = userLoginLog.getLoginTime();
            userInfoVO.setLastLoginTime(lastLoginTime);
        }
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
//        userAuthInfo.setTenantId(user.getTenantId());
        userAuthInfo.setUsername(user.getUsername());
        userAuthInfo.setPassword(user.getPassword());
        userAuthInfo.setRealName(user.getRealName());
        userAuthInfo.setStatus(user.getStatus());
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

        // deptId 和 deptName 映射
        List<SysDept> deptList = sysDeptService.list();
        Map<Long, String> deptIdNameMap = deptList.stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));
        
        // 机构ID和机构名称映射（只包含机构类型）
        Map<Long, String> orgIdNameMap = deptList.stream()
                .filter(dept -> OrganizationTypeEnum.ORGANIZATION.getValue().equals(dept.getDeptType()))
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));

        // userId 和 roleNames 映射
        Map<Long, List<String>> userIdRoleNamesMap = buildUserIdToRoleNamesMapping();

        List<UserExportVO> userExportVOS = userList.stream()
                .map(this::convertToUserExportVO)
                .peek(userExportVO -> {
                    // 角色名称
                    List<String> roleNames = userIdRoleNamesMap.getOrDefault(userExportVO.getUserId(), Collections.emptyList());
                    userExportVO.setRoleNames(String.join(",", roleNames));

                    // 部门名称
                    Long deptId = userExportVO.getDeptId();
                    if (deptId != null) {
                        String deptName = deptIdNameMap.getOrDefault(deptId, "");
                        userExportVO.setDeptName(deptName);
                        
                        // 机构名称（通过部门找上级机构）
                        String orgName = findOrgNameByDeptId(deptId, deptList);
                        userExportVO.setOrgName(orgName);
                    }
                })
                .collect(toList());

        return userExportVOS;
    }

    /**
     * 根据部门ID查找所属机构名称
     * @param deptId 部门ID
     * @param deptList 所有部门/机构列表
     * @return 机构名称
     */
    private String findOrgNameByDeptId(Long deptId, List<SysDept> deptList) {
        if (deptId == null) {
            return "";
        }
        
        // 创建ID到部门的映射
        Map<Long, SysDept> idDeptMap = deptList.stream()
                .collect(Collectors.toMap(SysDept::getId, dept -> dept));
        
        // 查找当前部门
        SysDept currentDept = idDeptMap.get(deptId);
        if (currentDept == null) {
            return "";
        }
        
        // 如果当前部门就是机构，直接返回名称
        if (OrganizationTypeEnum.ORGANIZATION.getValue().equals(currentDept.getDeptType())) {
            return currentDept.getDeptName();
        }
        
        // 如果当前是部门，查找其父级机构
        Long parentId = currentDept.getParentId();
        while (parentId != null && parentId != 0) {
            SysDept parentDept = idDeptMap.get(parentId);
            if (parentDept == null) {
                break;
            }
            
            // 如果父级是机构，返回机构名称
            if (OrganizationTypeEnum.ORGANIZATION.getValue().equals(parentDept.getDeptType())) {
                return parentDept.getDeptName();
            }
            
            // 继续向上查找
            parentId = parentDept.getParentId();
        }
        
        return "";
    }


    /**
     * 构建userId 和 roleNames 映射
     * @return
     */
    private Map<Long, List<String>> buildUserIdToRoleNamesMapping() {
        // 用户角色关联表
        List<SysUserRole> userRoleList = userRoleService.list();
        List<Long> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(toList());

        List<SysRole> roleList = userDataService.selectRoleList(roleIds);
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
        userExportVo.setDeptId(user.getDeptId()); // 设置部门ID

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
        sysUser.setAvatar(userInfo.getAvatar());
        sysUser.setGender(userInfo.getGender());
        sysUser.setMobile(userInfo.getMobile());
        sysUser.setEmail(userInfo.getEmail());
        return this.updateById(sysUser);
    }


}