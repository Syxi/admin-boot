package com.admin.web;

import com.admin.common.result.PageResult;
import com.admin.module.system.entity.UserOperationLog;
import com.admin.module.system.query.UserOperationLogQuery;
import com.admin.module.system.service.UserOperationLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户操作日志")
@RequestMapping("/log")
@RestController()
@AllArgsConstructor
public class UserOperationLogController {

    private final UserOperationLogService userOperationLogService;


    @Operation(summary = "操作日志列表")
    @GetMapping("/userOperationLog")
    public PageResult<UserOperationLog> selectUserOperationLogPage(UserOperationLogQuery query) {
        IPage<UserOperationLog> userOperationLogIPage = userOperationLogService.selectUserOperationLogPage(query);
        return PageResult.success(userOperationLogIPage);
    }

}
