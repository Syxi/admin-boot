package com.admin.module.system.service;

import com.admin.module.system.dto.PasswordUpdateDTO;
import com.admin.module.system.dto.UserAuthInfo;
import com.admin.module.system.dto.UserInfoUpdateDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysUser;
import com.admin.module.system.form.UserForm;
import com.admin.module.system.query.UserQuery;
import com.admin.module.system.vo.TransferVO;
import com.admin.common.excel.export.UserExportVO;
import com.admin.module.system.vo.UserInfoVO;
import com.admin.module.system.vo.UserVO;

import java.util.List;
import java.util.Set;

public interface SysUserService extends IService<SysUser> {


    /**
     * 获取用户分页列表
     * @param userQuery
     * @return
     */
    IPage<UserVO> selectUserPage(UserQuery userQuery);


    /**
     * 新增用户
     * @param userForm
     * @return
     */
    boolean saveUser(UserForm userForm);

    /**
     * 更新用户
     * @param userForm
     * @return
     */
    boolean updateUser(UserForm userForm);


    /**
     * 获取用户详情
     * @param userId
     * @return
     */
    UserForm getUserDetail(Long userId);


    /**
     * 批量删除用户
     * @param userIds
     * @return
     */
    boolean deleteBatchUsers(List<Long> userIds);


    /**
     * 启用用户
     * @param userIds
     * @return
     */
    boolean enableUser(List<Long> userIds);

    /**
     * 禁用用户
     * @param userIds
     * @return
     */
    boolean disableUser(List<Long> userIds);

    /**
     * 重置密码
     *
     * @param
     * @return
     */
    String resetPassword(List<Long> userIds);

    /**
     * 登录身份认证
     * @param username
     * @return
     */
    UserAuthInfo getUserAuthInfo(String username);

    /**
     * 导出用户列表
     * @param
     * @return
     */
    List<UserExportVO> exportUsers(List<Long> userIds);

    /**
     * 获取当前登录用户信息
     * @param username
     * @return
     */
    UserInfoVO getCurrentUserInfo(String username, Set<String> roleCodes);


    /**
     * 获取角色的用户列表
     * @param roleId
     * @return
     */
    List<TransferVO> selectUserListInRole(Long roleId);


    /**
     * 获取不属于该角色的用户列表
     * @param roleId
     * @return
     */
    List<TransferVO> selectUserListNotInRole(Long roleId);


    /**
     * 更新密码
     * @param userId
     * @return
     */
    ResultVO<Boolean> updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO);


    /**
     * 更新个人信息
     * @param userInfo
     * @return
     */
    boolean updateUserInfo(Long userId, UserInfoUpdateDTO userInfo);
}
