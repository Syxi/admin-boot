package com.admin.web;

import com.admin.common.result.ResultVO;
import com.admin.module.system.service.AuthService;
import com.admin.module.system.service.SysUserRoleService;
import com.admin.module.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author suYan
 * @date 2023/12/23 11:03
 */
@Slf4j
@Tag(name = "测试接口")
@RequiredArgsConstructor
@RestController
public class TestController {

    private final SysUserService sysUserService;

    private final SysUserRoleService sysUserRoleService;

    private final PasswordEncoder passwordEncoder;


    private final AuthService authService;

    @Operation(summary = "加密密码")
    @GetMapping("/api/encode/password")
    public ResultVO bcryptPasswordEncode(String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        // 加密密码
        String passwordEncode = bCryptPasswordEncoder.encode(password);
        return ResultVO.success(passwordEncode);
    }



}
