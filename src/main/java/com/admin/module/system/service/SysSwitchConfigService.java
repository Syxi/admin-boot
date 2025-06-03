package com.admin.module.system.service;

import com.admin.module.system.entity.SysSwitchConfig;
import com.admin.module.system.query.SysSwitchConfigQuery;
import com.admin.module.system.vo.SysSwitchConfigVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author sy
* @description 针对表【sys_switch_config(系统业务开关配置表)】的数据库操作Service
* @createDate 2024-08-28 15:12:59
*/
public interface SysSwitchConfigService extends IService<SysSwitchConfig> {



    /**
     * 初始化系统业务开关缓存
     * @return
     */
    boolean initSysSwitchConfigCache();

    /**
     * 系统业务开关分页列表
     * @return
     */
    IPage<SysSwitchConfigVO> selectSysSwitchConfigPage(SysSwitchConfigQuery config);

    /**
     * 更新系统配置业务信息
     * @param sysSwitchConfig
     * @return
     */
    boolean updateSysSwitchConfig(SysSwitchConfig sysSwitchConfig);

    /**
     * 启动或关闭系统配置
     * @param id
     * @return
     */
    boolean updateStatus(Long id, String configKey, Boolean configValue);
}
