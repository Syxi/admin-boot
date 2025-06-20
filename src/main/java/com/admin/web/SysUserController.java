package com.admin.web;

import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.excel.*;
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
import com.admin.module.system.vo.*;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

/**
 * @author suYan
 * @date 2023/4/2 13:27
 */
@Tag(name = "用户接口")
@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class SysUserController {

    private final SysUserService sysUserService;


    @Operation(summary = "用户分页列表")
    @GetMapping("/page")
    public PageResult<UserVO> selectUserPage(UserQuery userQuery) {
        IPage<UserVO> userVOList = sysUserService.selectUserPage(userQuery);
        return PageResult.success(userVOList);
    }


    @Operation(summary = "新增用户")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:user:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> addUser(@RequestBody UserForm userForm) {
        boolean result = sysUserService.saveUser(userForm);
        return ResultVO.judge(result);
    }



    @Operation(summary = "更新用户")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('sys:user:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> editUser(@RequestBody UserForm userForm) {
        boolean result = sysUserService.updateUser(userForm);
        return ResultVO.judge(result);
    }


    @Operation(summary = "获取用户详情")
    @GetMapping("/detail/{userId}")
    public ResultVO<UserForm> getUserDetail(@PathVariable("userId") @NotNull(message = "userId不能为空") Long userId) {
        UserForm userForm = sysUserService.getUserDetail(userId);
        return ResultVO.success(userForm);
    }




    @Operation(summary = "批量删除用户")
    @PreAuthorize("@pms.hasPerm('sys:user:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> batchRemoveUser(@RequestBody List<Long> userIds) {
         boolean result = sysUserService.deleteBatchUsers(userIds);
         return ResultVO.judge(result);
    }


    @Operation(summary = "启用用户")
    @PreAuthorize("@pms.hasPerm('sys:user:enable')")
    @PutMapping("/enable")
    public ResultVO<Boolean> enableUser(@RequestBody List<Long> userIds) {
        boolean result = sysUserService.enableUser(userIds);
        return ResultVO.judge(result);
    }

    @Operation(summary = "禁用用户")
    @PreAuthorize("@pms.hasPerm('sys:user:disable')")
    @PutMapping("disable")
    public ResultVO<Boolean> disableUser(@RequestBody List<Long> userIds) {
        boolean result = sysUserService.disableUser(userIds);
        return ResultVO.judge(result);
    }


    @Operation(summary = "重置密码")
    @PreAuthorize("@pms.hasPerm('sys:user:password')")
    @PutMapping("/resetPassword")
    public ResultVO<String> resetPassword(@RequestBody List<Long> userIds) {
        String newPassword = sysUserService.resetPassword(userIds);
        return ResultVO.success(newPassword);
    }


    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/userInfo")
    public ResultVO<UserInfoVO> getCurrentUserInfo() {
        String username = SecurityUtils.getCurrentUser().get().getUsername();
        Set<String> roleCodes = SecurityUtils.getRoleCodes();
        UserInfoVO userAuthInfo = sysUserService.getCurrentUserInfo(username, roleCodes);
        return ResultVO.success(userAuthInfo);
    }


    @Operation(summary = "Excel导入用户")
    @PreAuthorize("@pms.hasPerm('sys:user:import')")
    @PostMapping("/import")
    public ResultVO<ImportResult> importUsers(MultipartFile file) throws IOException {
        UserImportListener listener = new UserImportListener();
        ImportResult result = ExcelUtil.importExcel(file.getInputStream(), UserImportVO.class, listener);
        return ResultVO.success(result);
    }


    @Operation(summary = "Excel导出用户")
    @PreAuthorize("@pms.hasPerm('sys:user:export')")
    @PostMapping("/export")
    public void exportUsers(HttpServletResponse response, @RequestBody List<Long> userIds) throws IOException {
        String fileName = "用户列表.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        List<UserExportVO> userExportVOS = sysUserService.exportUsers(userIds);
        EasyExcel.write(response.getOutputStream(), UserExportVO.class).sheet("用户列表")
                .doWrite(userExportVOS);
    }


    @Operation(summary = "用户导入模板下载")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        String fileName = "用户导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        String fileClassPath = "templates" + File.separator + fileName;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileClassPath);

        ServletOutputStream outputStream = response.getOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(inputStream).build();
        excelWriter.finish();
    }

    @Operation(summary = "不属于角色的用户列表")
    @GetMapping("/notInRole/{roleId}")
    public ResultVO<List<TransferVO>> selectUserListNotinRole(@PathVariable("roleId") Long roleId) {
        List<TransferVO> transferVOList = sysUserService.selectUserListNotInRole(roleId);
        return ResultVO.success(transferVOList);
    }


    @Operation(summary = "属于角色的用户列表")
    @GetMapping("/inRole/{roleId}")
    public ResultVO<List<TransferVO>> selectUserListInRole(@PathVariable("roleId") Long roleId) {
        List<TransferVO> transferVOList = sysUserService.selectUserListInRole(roleId);
        return ResultVO.success(transferVOList);
    }



    @Operation(summary = "更新密码")
    @PostMapping("/updatePassword")
    public ResultVO<Boolean> updatePassword(@RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        Long userId = SecurityUtils.getUserId();
        return sysUserService.updatePassword(userId, passwordUpdateDTO);
    }



    @Operation(summary = "更新个人信息")
    @PutMapping("/update/me")
    public ResultVO<Boolean> updateUserInfo(@RequestBody UserInfoUpdateDTO userInfo) {
        Long userId = SecurityUtils.getUserId();
        boolean result = sysUserService.updateUserInfo(userId, userInfo);
        return ResultVO.success(result);
    }

}
