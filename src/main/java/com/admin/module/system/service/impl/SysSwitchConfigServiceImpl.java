package com.admin.module.system.service.impl;

import com.admin.module.system.entity.SysSwitchConfig;
import com.admin.module.system.mapper.SysSwitchConfigMapper;
import com.admin.module.system.query.SysSwitchConfigQuery;
import com.admin.module.system.service.SysSwitchConfigService;
import com.admin.module.system.vo.SysSwitchConfigVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
* @author sy
* @description 针对表【sys_switch_config(系统业务开关配置表)】的数据库操作Service实现
* @createDate 2024-08-28 15:12:59
*/

@RequiredArgsConstructor
@Slf4j
@Service
public class SysSwitchConfigServiceImpl extends ServiceImpl<SysSwitchConfigMapper, SysSwitchConfig> implements SysSwitchConfigService{

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 初始化系统业务开关缓存
     *
     * @return
     */
    @Override
    public boolean initSysSwitchConfigCache() {
        log.info("缓存系统业务开关开始");
        Instant startTime = Instant.now();
        List<SysSwitchConfig> sysSwitchConfigList = this.list();

        if (CollectionUtils.isNotEmpty(sysSwitchConfigList)) {
            for (SysSwitchConfig sysSwitchConfig : sysSwitchConfigList) {
                // 缓存
                redisTemplate.opsForValue().set(sysSwitchConfig.getConfigKey(), sysSwitchConfig.getConfigValue());
            }
        }

        log.info("缓存系统业务开关配置结束，耗时：{}", ChronoUnit.SECONDS.between(startTime, Instant.now()));
        return true;
    }

    /**
     * 系统业务开关分页列表
     *
     * @return
     */
    @Override
    public IPage<SysSwitchConfigVO> selectSysSwitchConfigPage(SysSwitchConfigQuery config) {
        LambdaQueryWrapper<SysSwitchConfig> queryWrapper = new LambdaQueryWrapper<SysSwitchConfig>();
        if (StringUtils.isNotEmpty(config.getConfName())) {
            queryWrapper.like(SysSwitchConfig::getConfigKey, config.getConfName());
        }
        Page<SysSwitchConfig> page = new Page<>(config.getPage(), config.getLimit());
        IPage<SysSwitchConfig> pageData = this.page(page, queryWrapper);

        IPage<SysSwitchConfigVO> switchConfigVOPage = pageData.convert(sysSwitchConfig -> {
            SysSwitchConfigVO sysSwitchConfigVO = new SysSwitchConfigVO();
            sysSwitchConfigVO.setId(sysSwitchConfig.getId());
            sysSwitchConfigVO.setConfigName(sysSwitchConfig.getConfigName());
            sysSwitchConfigVO.setConfigKey(sysSwitchConfig.getConfigKey());
            Boolean configValue = Boolean.parseBoolean(sysSwitchConfig.getConfigValue());
            sysSwitchConfigVO.setConfigValue(configValue);
            sysSwitchConfigVO.setRemark(sysSwitchConfig.getRemark());
            return sysSwitchConfigVO;
        });
        return switchConfigVOPage;
    }

    /**
     * 更新系统配置业务信息
     *
     * @param sysSwitchConfig
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateSysSwitchConfig(SysSwitchConfig sysSwitchConfig) {
        boolean result = this.updateById(sysSwitchConfig);
        return result;
    }

    /**
     * 启动或关闭系统配置
     *
     * @param id
     * @param configValue
     * @return
     */
    @Override
    public boolean updateStatus(Long id, String configKey, Boolean configValue) {
        String strConfigValue = Boolean.toString(configValue);
        LambdaUpdateWrapper<SysSwitchConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysSwitchConfig::getId, id);
        updateWrapper.set(SysSwitchConfig::getConfigValue, strConfigValue);
        boolean result = this.update(updateWrapper);

        // 更新缓存
        if (result) {
            redisTemplate.delete(configKey);
            redisTemplate.opsForValue().set(configKey, strConfigValue);
        }
        return result;
    }


}




