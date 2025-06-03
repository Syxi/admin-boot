package com.admin.module.system.service;

import com.admin.module.system.entity.SystemConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author sy
* @description 针对表【system_config(系统配置表)】的数据库操作Service
* @createDate 2024-08-28 15:12:59
*/
public interface SystemConfigService extends IService<SystemConfig> {



    /**
     * 初始化系统配置缓存
     * @return
     */
    boolean initSystemConfigCache();

}
