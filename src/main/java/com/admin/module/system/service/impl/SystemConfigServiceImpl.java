package com.admin.module.system.service.impl;

import com.admin.module.system.mapper.SystemConfigMapper;
import com.admin.module.system.entity.SystemConfig;
import com.admin.module.system.service.SystemConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
* @author sy
* @description 针对表【system_config(系统配置表)】的数据库操作Service实现
* @createDate 2024-08-28 15:12:59
*/

@Slf4j
@RequiredArgsConstructor
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService{


    private final RedisTemplate<String, String> redisTemplate;


    /**
     * 初始化系统配置缓存
     *
     * @return
     */
    @Override
    public boolean initSystemConfigCache() {
        log.info("缓存系统配置开始");
        Instant startTime = Instant.now();
        List<SystemConfig> systemConfigList = this.list();
        if (CollectionUtils.isNotEmpty(systemConfigList)) {
            for (SystemConfig systemConfig : systemConfigList) {
                redisTemplate.opsForValue().set(systemConfig.getConfigKey(), systemConfig.getConfigValue());
            }
        }

        log.info("缓存系统配置结束，耗时： {}", ChronoUnit.SECONDS.between(startTime, Instant.now()));
        return true;
    }
}




