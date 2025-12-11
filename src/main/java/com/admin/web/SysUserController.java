package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.excel.ExcelUtil;
import com.admin.common.excel.ImportResult;
import com.admin.common.excel.export.UserExportVO;
import com.admin.common.excel.importvo.UserImportVO;
import com.admin.common.excel.listener.UserImportListener;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.dto.PasswordUpdateDTO;
import com.admin.module.system.dto.UserInfoUpdateDTO;
import com.admin.module.system.form.UserForm;
import com.admin.module.system.query.UserQuery;
import com.admin.module.system.service.SysUserService;
import com.admin.module.system.vo.TransferVO;
import com.admin.module.system.vo.UserInfoVO;
import com.admin.module.system.vo.UserVO;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理控制器
 * 
 * @author suYan
 * @date 2023/4/2 13:27
 */
@Slf4j
@Tag(name = "用户接口")
@Validated
@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 用户分页列表
     *
     * @param userQuery 查询参数
     * @return 用户分页列表
     */
    @Operation(summary = "用户分页列表")
    @GetMapping("/page")
    public PageResult<UserVO> selectUserPage(UserQuery userQuery) {
        IPage<UserVO> userVOList = sysUserService.selectUserPage(userQuery);
        return PageResult.success(userVOList);
    }

    /**
     * 新增用户
     * 
     * @param userForm 用户表单数据
     * @return 操作结果
     */
    @Operation(summary = "新增用户")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:user:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> addUser(@Valid @RequestBody UserForm userForm) {
        boolean result = sysUserService.saveUser(userForm);
        return ResultVO.judge(result);
    }

    /**
     * 更新用户
     * 
     * @param userForm 用户表单数据
     * @return 操作结果
     */
    @Operation(summary = "更新用户")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:user:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> editUser(@Valid @RequestBody UserForm userForm) {
        boolean result = sysUserService.updateUser(userForm);
        return ResultVO.judge(result);
    }

    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/detail/{userId}")
    public ResultVO<UserForm> getUserDetail(
            @PathVariable("userId") @NotNull(message = "userId不能为空") Long userId) {
        UserForm userForm = sysUserService.getUserDetail(userId);
        return ResultVO.success(userForm);
    }

    /**
     * 批量删除用户
     * 
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除用户")
    @PreAuthorize("@pms.hasPerm('sys:user:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> batchRemoveUser(
            @RequestBody @NotEmpty(message = "用户ID列表不能为空") List<Long> userIds) {
        boolean result = sysUserService.deleteBatchUsers(userIds);
        return ResultVO.judge(result);
    }

    /**
     * 启用用户
     * 
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "启用用户")
    @PreAuthorize("@pms.hasPerm('sys:user:enable')")
    @PutMapping("/enable")
    public ResultVO<Boolean> enableUser(
            @RequestBody @NotEmpty(message = "用户ID列表不能为空") List<Long> userIds) {
        boolean result = sysUserService.enableUser(userIds);
        return ResultVO.judge(result);
    }

    /**
     * 禁用用户
     * 
     * @param userIds 用户ID列表
     * @return 操作结果
     */
    @Operation(summary = "禁用用户")
    @PreAuthorize("@pms.hasPerm('sys:user:disable')")
    @PutMapping("/disable")
    public ResultVO<Boolean> disableUser(
            @RequestBody @NotEmpty(message = "用户ID列表不能为空") List<Long> userIds) {
        boolean result = sysUserService.disableUser(userIds);
        return ResultVO.judge(result);
    }

    /**
     * 重置密码
     * 
     * @param userIds 用户ID列表
     * @return 新密码
     */
    @Operation(summary = "重置密码")
    @PreAuthorize("@pms.hasPerm('sys:user:password')")
    @PutMapping("/resetPassword")
    public ResultVO<String> resetPassword(
            @RequestBody @NotEmpty(message = "用户ID列表不能为空") List<Long> userIds) {
        String newPassword = sysUserService.resetPassword(userIds);
        return ResultVO.success(newPassword);
    }

    /**
     * 获取当前登录用户信息
     * 
     * @return 用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/userInfo")
    public ResultVO<UserInfoVO> getCurrentUserInfo() {
        String username = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("未获取到当前用户信息"))
                .getUsername();
        UserInfoVO userAuthInfo = sysUserService.getCurrentUserInfo(username);
        return ResultVO.success(userAuthInfo);
    }

    /**
     * Excel导入用户
     * 
     * @param file Excel文件
     * @return 导入结果
     * @throws IOException IO异常
     */
    @Operation(summary = "Excel导入用户")
    @PreAuthorize("@pms.hasPerm('sys:user:import')")
    @PostMapping("/import")
    public ResultVO<ImportResult> importUsers(
            @RequestParam("file") MultipartFile file) throws IOException {
        try {
            UserImportListener listener = new UserImportListener();
            ImportResult result = ExcelUtil.importExcel(
                    file.getInputStream(), UserImportVO.class, listener
            );
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("导入用户失败", e);
            return ResultVO.error("导入用户失败: " + e.getMessage());
        }
    }

    /**
     * Excel导出用户
     * 
     * @param response HTTP响应
     * @param userIds 用户ID列表（为空则导出所有）
     * @throws IOException IO异常
     */
    @Operation(summary = "Excel导出用户")
    @PreAuthorize("@pms.hasPerm('sys:user:export')")
    @PostMapping("/export")
    public void exportUsers(
            HttpServletResponse response,
            @RequestBody(required = false) List<Long> userIds) throws IOException {
        String fileName = "用户列表.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", 
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        List<UserExportVO> userExportVOS = sysUserService.exportUsers(userIds);
        EasyExcel.write(response.getOutputStream(), UserExportVO.class)
                .sheet("用户列表")
                .doWrite(userExportVOS);
    }

    /**
     * 用户导入模板下载
     * 
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    @Operation(summary = "用户导入模板下载")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        String fileName = "用户导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", 
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        String fileClassPath = "templates" + File.separator + fileName;
        try (InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(fileClassPath);
             ServletOutputStream outputStream = response.getOutputStream()) {
            
            if (inputStream == null) {
                throw new IOException("模板文件不存在: " + fileClassPath);
            }
            
            ExcelWriter excelWriter = EasyExcel.write(outputStream)
                    .withTemplate(inputStream)
                    .build();
            excelWriter.finish();
        }
    }

    /**
     * 获取不属于指定角色的用户列表
     * 
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Operation(summary = "不属于角色的用户列表")
    @GetMapping("/notInRole/{roleId}")
    public ResultVO<List<TransferVO>> selectUserListNotInRole(
            @PathVariable("roleId") @NotNull(message = "角色ID不能为空") Long roleId) {
        List<TransferVO> transferVOList = sysUserService.selectUserListNotInRole(roleId);
        return ResultVO.success(transferVOList);
    }

    /**
     * 获取属于指定角色的用户列表
     * 
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Operation(summary = "属于角色的用户列表")
    @GetMapping("/inRole/{roleId}")
    public ResultVO<List<TransferVO>> selectUserListInRole(
            @PathVariable("roleId") @NotNull(message = "角色ID不能为空") Long roleId) {
        List<TransferVO> transferVOList = sysUserService.selectUserListInRole(roleId);
        return ResultVO.success(transferVOList);
    }

    /**
     * 更新密码
     * 
     * @param passwordUpdateDTO 密码更新数据
     * @return 操作结果
     */
    @Operation(summary = "更新密码")
    @PostMapping("/updatePassword")
    public ResultVO<Boolean> updatePassword(
            @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        Long userId = SecurityUtils.getUserId();
        return sysUserService.updatePassword(userId, passwordUpdateDTO);
    }

    /**
     * 更新个人信息
     * 
     * @param userInfo 用户信息
     * @return 操作结果
     */
    @Operation(summary = "更新个人信息")
    @PutMapping("/update/me")
    public ResultVO<Boolean> updateUserInfo(
            @Valid @RequestBody UserInfoUpdateDTO userInfo) {
        Long userId = SecurityUtils.getUserId();
        boolean result = sysUserService.updateUserInfo(userId, userInfo);
        return ResultVO.success(result);
    }
}
