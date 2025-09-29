package com.admin.module.system.service;

import com.admin.module.system.entity.Platform;
import com.admin.module.system.vo.PlatformVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 苏彦
* @description 针对表【t_platform(平台系统表)】的数据库操作Service
* @createDate 2025-09-25 15:33:49
*/
public interface PlatformService extends IService<Platform> {

    /**
     * 平台系统分页列表
     * @return
     */
    IPage<PlatformVO> selectPage(PlatformVO platformVO);

    /**
     * 新增平台系统
     * @param platform
     * @return
     */
    boolean savePlatform(Platform platform);

    /**
     * 更新平台系统
     * @param platform
     * @return
     */
    boolean updatePlatform(Platform platform);

    /**
     * 查询平台系统首页列表
     * @return
     */
    List<Platform> selectPlatformList();
}
