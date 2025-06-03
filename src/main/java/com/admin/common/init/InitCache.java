package com.admin.common.init;

import com.admin.module.system.service.SysSwitchConfigService;
import com.admin.module.system.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 系统启动后，初始化缓存
 */

@Slf4j
@RequiredArgsConstructor
@Component
public class InitCache implements CommandLineRunner {

    private final SystemConfigService systemConfigService;

    private final SysSwitchConfigService sysSwitchConfigService;

    @Override
    public void run(String... args) throws Exception {

        // 系统配置表缓存
        CompletableFuture.runAsync(() -> {
            systemConfigService.initSystemConfigCache();
        });

        CompletableFuture.runAsync(() -> {
            sysSwitchConfigService.initSysSwitchConfigCache();
        });
    }
}
