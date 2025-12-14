package com.admin.module.system.service;

import java.util.List;

/**
 * 用户Token刷新服务
 * 专门处理用户Token刷新相关逻辑，避免循环依赖
 */
public interface UserTokenRefreshService {
    
    /**
     * 根据用户ID列表获取用户名
     *
     * @param userIds 用户ID列表
     * @return 用户名列表
     */
    List<String> getUsernamesByIds(List<Long> userIds);
}