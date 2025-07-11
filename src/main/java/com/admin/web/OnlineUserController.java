package com.admin.web;

import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.query.OnlineUserQuery;
import com.admin.module.system.service.OnlineUserService;
import com.admin.module.system.vo.OnlineUserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onLineUser")
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    @Operation(summary = "分页查询在线用户", description = "在线用户超过1000")
    @GetMapping("/page")
    public PageResult<OnlineUserVO> getOnlineUserList(OnlineUserQuery onlineUserQuery) {
        IPage<OnlineUserVO> onlineUserVOIPage = onlineUserService.selectOnlineUserPage(onlineUserQuery);
        return PageResult.success(onlineUserVOIPage);
    }

    @DeleteMapping("/forceLogout/{username}")
    public ResultVO<Void> forceLogout(@PathVariable("username") String username) {
        onlineUserService.forceLogout(username);
        return ResultVO.success();
    }
}
