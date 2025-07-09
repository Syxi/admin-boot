package com.admin.web;

import com.admin.common.result.ResultVO;
import com.admin.module.system.query.OnlineUserQuery;
import com.admin.module.system.service.OnlineUserService;
import com.admin.module.system.vo.OnlineUserVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onLineUser")
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    @GetMapping("/page")
    public ResultVO<IPage<OnlineUserVO>> getOnlineUserList(OnlineUserQuery onlineUserQuery) {
        IPage<OnlineUserVO> page = onlineUserService.selectOnlineUserPage(onlineUserQuery);
        return ResultVO.success(page);
    }

    @GetMapping("/forceLogout")
    public ResultVO<Void> forceLogout(@RequestParam String username) {
        onlineUserService.forceLogout(username);
        return ResultVO.success();
    }
}
