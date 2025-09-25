package com.admin.web;

import com.admin.common.result.ResultVO;
import com.admin.module.system.service.impl.SystemInfoService;
import com.admin.module.system.vo.SystemInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SystemInfoController {

    private final SystemInfoService systemInfoService;

    @GetMapping("/systemInfo")
    public ResultVO<SystemInfoVO> getSystemInfo() {
        SystemInfoVO systemInfoVO = systemInfoService.getSystemInfoVO();
        return ResultVO.success(systemInfoVO);
    }

}
