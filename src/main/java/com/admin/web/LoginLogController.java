package com.admin.web;

import com.admin.common.result.PageResult;
import com.admin.module.system.entity.UserLoginLog;
import com.admin.module.system.service.UserLoginLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户登录日志")
@RequestMapping("/api")
@RestController
@AllArgsConstructor
public class LoginLogController {

    private final UserLoginLogService userLoginLogService;


    @Operation(summary = "用户登录日志列表")
    @GetMapping("/loginLog")
    public PageResult<UserLoginLog> loginLogPage(Integer page, Integer limit, String username) {
        IPage<UserLoginLog> loginLogIPage = userLoginLogService.selectLoginLogPage(page, limit, username);
        return PageResult.success(loginLogIPage);
    }
}
